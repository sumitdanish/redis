package com.example.demo;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import redis.clients.jedis.JedisPoolConfig;

@SpringBootApplication
@ComponentScan
public class DemoRedisExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoRedisExampleApplication.class, args);
	}
}

@Configuration
class RedisConfig {
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		System.out.println("Redis....");
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxTotal(5);
		jedisPoolConfig.setTestOnBorrow(true);
		jedisPoolConfig.setTestOnReturn(true);
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(jedisPoolConfig);
		return jedisConnectionFactory;
	}

	@Bean
	public RedisTemplate<String, Employee> redisTemplate() {
		final RedisTemplate<String, Employee> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory());
		
		return template;
	}
}

class Employee implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String id;

	public Employee(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public Employee() {

	}

	public String getName() {
		return this.name;
	}

	public String getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Employee other = (Employee) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id + " - " + name;
	}
}

@Transactional
@Repository
class EmployeeDAO {
	private final String KEY = "employee";

	@Autowired
	private RedisTemplate<String, Employee> redisTemplate;

	public Long addEmployee(Employee emp) {
		return redisTemplate.opsForList().leftPush(KEY, emp);
	}

	public long getNumberOfEmployee() {
		return redisTemplate.opsForList().size(KEY);
	}

	public Employee getEmployee(int index) {
		return redisTemplate.opsForList().index(KEY, index);
	}

	public void removeEmployee(Employee emp) {
		redisTemplate.opsForList().remove(KEY, 1, emp);
	}
}

@Service
class EmployeeService {

	@Autowired
	private EmployeeDAO empDAO;

	public Long addEmp(Employee emp) {
		return empDAO.addEmployee(emp);
	}

	public Employee getEmployee(int index) {
		return empDAO.getEmployee(index);
	}

	public void remove(Employee emp) {
		empDAO.removeEmployee(emp);
	}
}

@RestController
class EmployeeController {
	@Autowired
	private EmployeeService employeeService;

	@PostMapping(value = "add")
	public Long addEmployee(@RequestBody Employee emp) {
		return employeeService.addEmp(emp);
	}
	
	@GetMapping(value="get/{index}")
	public Employee getEmployee(@PathVariable("index") int index) {
		return employeeService.getEmployee(index);
	}
}
