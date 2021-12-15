# eForce21 library for proper binary handling #

## Concept

Simple API to store and retrieve binaries in a streaming way with basic metadata (name, size, mime) management. Comes
with 2 backend-implementations allowing you to store the actual binaries in a filesystem folder or relational database
based on jpa/spring-template.

## Setup

Choose your backend and create service:

```
@Bean
public BinDataRepo binRepo(@Autowired JdbcTemplate jt, @Value("${spring.datasource.jdbc-url}") String jdbcUrl) {
    // Store in FS
    return new BinDataRepoFs(Paths.get("/path/to/storage"));
    
    // Store in DB (MySQL, H2)
    return new BinDataRepoDb(jt);
    
    // Store in DB (MsSql)
    return new BinDataRepoDbMssql(jt);
}

@Bean
public BinFileService binService() {
    return new BinFileServiceImpl();
}
```

Entity and repository:

```
// Init Spring-Data-JPA-Repositories
@EnableJpaRepositories(basePackageClasses = {..., EforceBinModule.class}, ...)

// Register entities (somehow like this)
@Bean
@Primary
public LocalContainerEntityManagerFactoryBean defaultEntityManagerFactory(final EntityManagerFactoryBuilder builder) {
    return builder.dataSource(defaultDataSource()).packages(..., EforceBinModule.class).persistenceUnit(DEFAULT_PERSISTENCE_UNIT_NAME).build();
}
```

Flyway (mssql example):

```
create table bin_data (
  id varchar(255) not null,
  data varbinary(MAX),
  primary key (id)
);

create table bin_file (
  id bigint identity not null,
  data_id varchar(255) not null,
  mime varchar(255) not null,
  name varchar(255) not null,
  size bigint not null,
  ts_create bigint not null,
  cover bit not null,
  primary key (id)
);

# always bind own domain via join table:  
create table my_domain_table_files (
  my_domain_table_id bigint not null,
  fk_bin_file_id bigint not null,
  primary key (my_domain_table_id, fk_bin_file_id)
);

alter table my_domain_table_files add constraint 123 foreign key (fk_bin_file_id) references bin_file;
alter table my_domain_table_files add constraint 456 foreign key (my_domain_table_id) references my_domain_table_files;
```

## Usage

Take a look at package com.eforce21.lib.bin.file.example.
