package com.reyes.tutorial;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootTutorial03ApplicationTests {

	@Autowired
	private DataSource datasource;
	
	@Test
	public void contextLoads() throws SQLException {
		System.out.println(datasource);
		Connection cnn = datasource.getConnection();
		System.out.println(cnn);
		
		cnn.close();
		
	}

}
