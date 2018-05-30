package me.karun.migration.graph.model.store

import org.neo4j.driver.v1.{AuthTokens, GraphDatabase, StatementResult}

object GraphStore {
  // TODO: read from config
  private val url = "bolt://localhost:7687"

  private val user = "neo4j"
  private val pass = "neo4j1"
  private val session = GraphDatabase.driver(url, AuthTokens.basic(user, pass)).session

  def run(query: String): Boolean = try {
    execute(query)
    true
  } catch {
    case t: Throwable => handleError(query, t)
  }

  def execute(query: String): StatementResult = session.run(query)

  private def handleError(query: String, t: Throwable) = {
    println(s"! Error while executing '$query'")
    println(s"! Error message: ${t.printStackTrace()}")
    false
  }
}
