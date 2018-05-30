package me.karun.migration.graph

import me.karun.migration.graph.MigrationYamlProtocol._
import me.karun.migration.graph.model.Migration
import net.jcazevedo.moultingyaml._

import scala.io.Source

object Application extends App {
  println("Starting migration") // TODO: Migrate all println statements to slf4j

  val migrationPlan = Source.fromFile("src/main/resources/migrations.yaml")
    .mkString
    .parseYaml
    .convertTo[List[Migration]]

  val status = migrationPlan
    .filter(_.isExecutable)
    .map(_.execute())
    .reduceOption(_ || _)
    .getOrElse(true) // TODO: Migrate to a status enum with values (Complete, No-Op, Rollback)
  // TODO: Stop migration on rollback

  println(s"Executed with status = $status")
}