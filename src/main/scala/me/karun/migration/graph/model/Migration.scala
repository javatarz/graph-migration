package me.karun.migration.graph.model

import java.security.MessageDigest

import me.karun.migration.graph.model.store.GraphStore

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

case class Migration(version: String, author: String, query: String, rollback: Option[String]) {
  def isExecutable: Boolean = {
    val query =
      s"""
         |MATCH (mp:MigrationPlan)-[e:has_executed]->(m:Migration)
         |WHERE m.version = '$version'
         |RETURN m.hash as hash
       """.stripMargin
    val results = GraphStore.execute(query)
      .list()
      .asScala
      .map(r => r.get("hash"))

    if (results.isEmpty) {
      true
    } else if (results.size == 1) {
      if (results.head.asString().equals(hash())) {
        false
      } else {
        throw new RuntimeException(s"Hash check violated for ${shortLog()} because db.hash (${results.head.asString()}) != migration.hash (${hash()})")
      }
    } else {
      throw new RuntimeException(s"Duplicate migration nodes exist for ${shortLog()}. Invalid data structure. Stopping execution")
    }
  }

  def execute(): Boolean = {
    println(s"> Executing ${shortLog()} => $query")
    val retVal = if (GraphStore.run(query)) {
      logSuccessfulMigration()
    } else {
      initiateRollback(s"Unable to perform $this")
    }

    println
    retVal
  }

  private def logSuccessfulMigration(): Boolean = {
    val createMigrationPlan =
      s"""
         |MERGE (mp:MigrationPlan)
         |ON CREATE SET mp.lastMigrationVersion = '$version'
         |ON MATCH SET mp.lastMigrationVersion = '$version'
         |ON CREATE SET mp.lastMigrationAuthor = '$author'
         |ON MATCH SET mp.lastMigrationAuthor = '$author'
         |ON CREATE SET mp.lastMigrationHash = '${hash()}'
         |ON MATCH SET mp.lastMigrationHash = '${hash()}';
    """.stripMargin

    val createMigration = s"CREATE (m:Migration {version:'$version', author:'$author', hash:'${hash()}'});"
    val createEdge = s"MATCH (mp:MigrationPlan), (m:Migration) WHERE (m.version='$version') CREATE (mp)-[e:has_executed {}]->(m);"

    println(s">> ${shortLog()} was successful. Logging status to data store")
    if (GraphStore.run(createMigrationPlan)
      && GraphStore.run(createMigration)
      && GraphStore.run(createEdge)) {
      true
    } else {
      initiateRollback(s"Unable to persist migration status for $this")
    }
  }

  private def shortLog(): String = s"Migration v$version by $author"

  private def initiateRollback(reason: String) = {
    println(reason)
    val rollbackStatus = rollback.forall(GraphStore.run(_))

    if (rollbackStatus) false
    else throw new RuntimeException(s"$this failed to be applied")
  }

  private def hash() = MessageDigest.getInstance("MD5")
    .digest(toString.getBytes)
    .map(0xFF & _)
    .map("%02x".format(_))
    .foldLeft("") {
      _ + _
    }
}

/*

MERGE (mp:MigrationPlan)
ON CREATE SET mp.lastMigrationVersion = '1.0'
ON MERGE SET mp.lastMigrationVersion = '1.0'
ON CREATE SET mp.lastMigrationAuthor = 'karun'
ON MERGE SET mp.lastMigrationAuthor = 'karun'
ON CREATE SET mp.lastMigrationHash = "a";
ON MERGE SET mp.lastMigrationHash = "a";

create (migration:Migration {version:'1.0', author:'karun', query:"Q1", rollback:"R1"})

match (mp:MigrationPlan), (m:Migration)
 where (m.version='1.0')
 create (mp)-[e:has_executed {}]->(m)

create (migration:Migration {version:'2.0', author:'karun', query:"Q2", rollback:"R2"})

match (mp:MigrationPlan), (m:Migration)
 where (m.version='2.0')
 create (mp)-[e:has_executed {}]->(m)
*/