package com.reyes.tutorial.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
	
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
	
	

}
