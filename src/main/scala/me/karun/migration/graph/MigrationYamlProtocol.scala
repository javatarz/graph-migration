package me.karun.migration.graph

import me.karun.migration.graph.model.Migration
import net.jcazevedo.moultingyaml.DefaultYamlProtocol

object MigrationYamlProtocol extends DefaultYamlProtocol {
  implicit val migrationFormat = yamlFormat4(Migration)
}
