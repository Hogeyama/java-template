package com.example.demo.crosscutting;

import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {

  @Bean
  public DSLContext dsl(DataSource dataSource) {
    return DSL.using(dataSource, SQLDialect.POSTGRES);
  }
}
