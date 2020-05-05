#### spring boot use jdbc 
- 自動配置  
  ```
  預設的connection pool設定
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class

  PooledDataSourceConfiguration
  static class PooledDataSourceAvailableCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("PooledDataSource");
        if (DataSourceBuilder.findType(context.getClassLoader()) != null) {
            return ConditionOutcome.match(message.foundExactly("supported DataSource"));
		}
		return ConditionOutcome.noMatch(message.didNotFind("supported DataSource").atAll());
	}
  }
  ```

- 連線池，spring boot 2.x預設連線池com.zaxxer.hikari.HikariDataSource  
  ```
  DataSourceBuilder<T extends DataSource>
  private static final String[] DATA_SOURCE_TYPE_NAMES = new String[] { "com.zaxxer.hikari.HikariDataSource", "org.apache.tomcat.jdbc.pool.DataSource", "org.apache.commons.dbcp2.BasicDataSource" };

  The auto-configuration first tries to find and configure HikariCP.
  If HikariCP is available, it always choose it. Otherwise, if the Tomcat pooling is found, it is configured.
  ```

- spring boot 支持的配置  
  - DataSource配置  
    DataSourceConfiguration.class  
    ```
    ConditionalOnProperty(name = "spring.datasource.type", havingValue = "org.apache.tomcat.jdbc.pool.DataSource"

    ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.zaxxer.hikari.HikariDataSource"

    ...
    ```
  - 默認  
    org.apache.tomcat.jdbc.pool.DataSource.class

  - 根據  
    spring.datasource.type，可以異動datasource type

  - 可以自訂義  
    Generic DataSource configuration  
- DataSourceInitializationConfiguration.class，啟動數據源後的一系列動作，在DataSourceInitializerInvoker.class
  ```
    beanDefinition.setBeanClass(DataSourceInitializerPostProcessor.class);
    this.beanFactory.getBean(DataSourceInitializerInvoker.class);
	  	
    @Override
    public void afterPropertiesSet() {
		DataSourceInitializer initializer = getDataSourceInitializer();
		if (initializer != null) {
			boolean schemaCreated = this.dataSourceInitializer.createSchema();
			if (schemaCreated) {
				initialize(initializer);
			}
		}
	}
				
	以下兩者的getScripts
	if (resources != null) {
		return getResources(propertyName, resources, true);
	}

	可以設定resources路徑，如果查詢的到，就會依此讀取，不然就會使用預設值
				
	-> this.dataSourceInitializer.createSchema
	-> properties: DataSourceProperties.class即spring.datasource的設定

	boolean createSchema() {
		List<Resource> scripts = getScripts("spring.datasource.schema", this.properties.getSchema(), "schema");
		if (!scripts.isEmpty()) {
			if (!isEnabled()) {
	    		logger.debug("Initialization disabled (not running DDL scripts)");
				return false;
			}
			String username = this.properties.getSchemaUsername();
			String password = this.properties.getSchemaPassword();
			runScripts(scripts, username, password);
		}
		return !scripts.isEmpty();
	}
						
	-> getScripts("spring.datasource.schema", this.properties.getSchema(), "schema")
	-> 預設fallback: schema；platform: 預設all

	fallbackResources.add("classpath*:" + fallback + "-" + platform + ".sql");
	fallbackResources.add("classpath*:" + fallback + ".sql");
	initschema: schema-all.sql or schema.sql
				
	@Override
	public void onApplicationEvent(DataSourceSchemaCreatedEvent event) {
		// NOTE the event can happen more than once and
		// the event datasource is not used here
		DataSourceInitializer initializer = getDataSourceInitializer();
		if (!this.initialized && initializer != null) {
			initializer.initSchema();
			this.initialized = true;
		}
	}
				
	-> getScripts("spring.datasource.data", this.properties.getData(), "data");
	-> 預設fallback: data；platform: 預設all
	fallbackResources.add("classpath*:" + fallback + "-" + platform + ".sql");
	fallbackResources.add("classpath*:" + fallback + ".sql");
	initSchema: data-all.sql or data.sql
  ```

- 上述自動加載，在2.x需要initialization-mode: 啟用，always
  
- 上述設定application設定方式  
  ```
  spring:
  profiles:
  - init
  datasource:
    initialization-mode: always
    schema:
    - classpath:user.sql
    - classpath:department.sql
    data:
    - classpath:user-init-data.sql
    - classpath:department-init-data.sql
  ```

- 如果有數據源的話，jdbc套件會有這樣的設定 JdbcTemplateAutoConfiguration.class -> 設定 JdbcTemplateConfiguration.class
  
- 當系統啟動後，注入JdbcTemplate，因此直接在Dao層，可以直接呼叫使用  
  ```
  示範代碼(非真正在Dao層呼叫)

  @Autowired
  private JdbcTemplate jdbc;
	
  @GetMapping("/hello")
  public Map<String, String> list(){
	Map<String, String> val = new HashMap<>();
		
	List<Map<String,Object>> list = jdbc.queryForList("select * from department ");
		if(list != null){
			val.put("1", String.valueOf(list.get(0).get("name")));
			val.put("2", String.valueOf(list.get(1).get("name")));
		}
		
	return val;
  }
  ```