# Graph Migration

Graph migration utility helps users perform version controlled graph refactoring (like [liquibase](http://www.liquibase.org/) and [flyway](https://flywaydb.org/getstarted/) do for RDBMS)

## How to

Write your migrations in `migrations.yaml`

```

-
  version: "1.0"
  author: karun
  query: "CREATE (user:Users {name:'Karun',last_name:'AB',tokens:3})"
  rollback: "MATCH (u:Users) WHERE u.name = 'Karun' DELETE u"
-
  version: "2.0"
  author: karun
  query: "CREATE (user:Users {name:'Mansi',last_name:'Vartak',tokens:6})"
  rollback: "MATCH (u:Users) WHERE u.name = 'Mansi' DELETE u"
-
  version: "3.0"
  author: karun
  query: "CREATE (user:Users {name:'Bim',last_name:'Japhet',tokens:9})"
  rollback: "MATCH (u:Users) WHERE u.name = 'Bim' DELETE u"
```

## Roadmap

Further development will be done based on requirements from users. Here's some of the planned work. If you have a usecase (or would like one of these implemented), contribute or start a conversation (by creating a github issue for the feature request)! I'd love to hear from you. Open Source projects need users to live!

1. Use `slf4j` for logging
2. Provide migration status as an `enum` instead of a `Boolean`
3. Stop migrations if there is a rollback
4. Package the code and publish an ivy artifact