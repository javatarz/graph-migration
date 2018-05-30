package me.karun.migration.graph

import me.karun.migration.graph.MigrationYamlProtocol._
import me.karun.migration.graph.model.Migration
import net.jcazevedo.moultingyaml._

object Application extends App {
  println("Starting migration") // TODO: Migrate all println statements to slf4j

  // TODO: Read from dedicated yaml file
  val yaml =
    """
      |-
      |  version: "1.0"
      |  author: karun
      |  query: "CREATE (user:Users {name:'Karun',last_name:'AB',tokens:3})"
      |  rollback: "MATCH (u:Users) WHERE u.name = 'Karun' DELETE u"
      |-
      |  version: "2.0"
      |  author: karun
      |  query: "CREATE (user:Users {name:'Mansi',last_name:'Vartak',tokens:6})"
      |  rollback: "MATCH (u:Users) WHERE u.name = 'Mansi' DELETE u"
      |-
      |  version: "3.0"
      |  author: karun
      |  query: "CREATE (user:Users {name:'Bim',last_name:'Japhet',tokens:6})"
      |  rollback: "MATCH (u:Users) WHERE u.name = 'Bim' DELETE u"
    """.stripMargin.parseYaml
  val migrationPlan = yaml.convertTo[List[Migration]]

  val status = migrationPlan
    .filter(_.isExecutable)
    .map(_.execute())
    .reduceOption(_ || _)
    .getOrElse(true) // TODO: Migrate to a status enum with values (Complete, No-Op, Rollback)
  // TODO: Stop migration on rollback

  println(s"Executed with status = $status")
}