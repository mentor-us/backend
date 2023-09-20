package com.hcmus.mentor.backend;

import com.hcmus.mentor.backend.config.AppProperties;
import com.hcmus.mentor.backend.entity.Message;
import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.repository.MeetingRepository;
import com.hcmus.mentor.backend.repository.MessageRepository;
import com.hcmus.mentor.backend.repository.TaskRepository;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.StorageService;
import com.hcmus.mentor.backend.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.*;

@SpringBootApplication
@EnableSwagger2
@EnableConfigurationProperties(AppProperties.class)
public class SpringSocialApplication implements CommandLineRunner {

	private final StorageService storageService;
	private final MeetingRepository meetingRepository;
	private final TaskRepository taskRepository;
	private final MessageRepository messageRepository;
	private final UserService userService;
	private final AppProperties appProperties;
	private final UserRepository userRepository;
	private final MongoTemplate mongoTemplate;

	public SpringSocialApplication(StorageService storageService, MeetingRepository meetingRepository, TaskRepository taskRepository, MessageRepository messageRepository, UserService userService, AppProperties appProperties, UserRepository userRepository, MongoTemplate mongoTemplate) {
		this.storageService = storageService;
		this.meetingRepository = meetingRepository;
		this.taskRepository = taskRepository;
		this.messageRepository = messageRepository;
		this.userService = userService;
		this.appProperties = appProperties;
		this.userRepository = userRepository;
		this.mongoTemplate = mongoTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringSocialApplication.class, args);
	}

	private String genToken(String id, String username, String email) {
		User data1 = User.builder()
				.id(id)
				.name(username)
				.email(email)
				.build();
		User dtn = userRepository.save(data1);
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());
		String secret = appProperties.getAuth().getTokenSecret();
		return Jwts.builder()
				.setSubject(dtn.getId())
				.setIssuedAt(new Date())
				.setExpiration(expiryDate)
				.signWith(SignatureAlgorithm.HS512, secret)
				.compact();
	}

	@Override
	public void run(String... args) throws Exception {
		storageService.init();

//		Map<String, String> tokens = new HashMap<>();
//		List<String> emails = Arrays.asList(
//				"lqvu@fit.hcmus.edu.vn",
//				"19120302@student.hcmus.edu.vn",
//				"19120477@student.hcmus.edu.vn",
//				"19120483@student.hcmus.edu.vn",
//				"19120484@student.hcmus.edu.vn",
//				"19120492@student.hcmus.edu.vn",
//				"19120495@student.hcmus.edu.vn",
//				"nnduy8501@gmail.com"
//		);
//
//		String token1 = genToken("64232932707faa54d80286fd", "Doan Thu Ngan", "19120302@student.hcmus.edu.vn");
//		tokens.put("19120302@student.hcmus.edu.vn", token1);
//
//		String token2 = genToken("64232932707faa54d80286fe", "Le Van Dinh", "19120477@student.hcmus.edu.vn");
//		tokens.put("19120477@student.hcmus.edu.vn", token2);
//
//		String token3 = genToken("64232932707faa54d80286ff", "Thoi Hai Duc", "19120483@student.hcmus.edu.vn");
//		tokens.put("19120483@student.hcmus.edu.vn", token3);
//
//		String token4 = genToken("64232932707faa54d8028700", "Tram Huu Duc", "19120484@student.hcmus.edu.vn");
//		tokens.put("19120484@student.hcmus.edu.vn", token4);
//
//		String token5 = genToken("64232932707faa54d8028701", "Do Thai Duy", "19120492@student.hcmus.edu.vn");
//		tokens.put("19120492@student.hcmus.edu.vn", token5);
//
//		String token6 = genToken("64232932707faa54d8028702", "Nguyen Nhat Duy", "19120495@student.hcmus.edu.vn");
//		tokens.put("19120495@student.hcmus.edu.vn", token6);
//
//		String token7 = genToken("64232932707faa54d8028703", "Lam Quang Vu", "lqvu@fit.hcmus.edu.vn");
//		tokens.put("lqvu@fit.hcmus.edu.vn", token7);
//
//		// Admin
//		User data1 = User.builder()
//				.id("642370af5a2e8549bb408a5a")
//				.name("Admin")
//				.email("nnduy8501@gmail.com")
//				.roles(Arrays.asList("ROLE_USER", "ROLE_ADMIN"))
//				.build();
//		User dtn = userRepository.save(data1);
//		Date now = new Date();
//		Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());
//		String secret = appProperties.getAuth().getTokenSecret();
//		String token8 = Jwts.builder()
//				.setSubject(dtn.getId())
//				.setIssuedAt(new Date())
//				.setExpiration(expiryDate)
//				.signWith(SignatureAlgorithm.HS512, secret)
//				.compact();
//		tokens.put("nnduy8501@gmail.com", token8);
//
//		tokens.entrySet().forEach(set -> System.out.println(set.getKey() + " : " + set.getValue()));

//		Message message1 = Message.builder()
//				.senderId("637b48a0edfbaa57cea2d696")
//				.content("Hello from message1")
//				.createdDate(new Date())
//				.type(MessageType.SERVER)
//				.groupId("63f74a7cfc73ec6683c2d227")
//				.build();
//		Message message2 = Message.builder()
//				.senderId("637b48a0edfbaa57cea2d696")
//				.content("Hello from message2")
//				.createdDate(new Date())
//				.type(MessageType.SERVER)
//				.groupId("63f74a7cfc73ec6683c2d227")
//				.build();
//		Message message3 = Message.builder()
//				.senderId("637b48a0edfbaa57cea2d696")
//				.content("Hello from message3")
//				.createdDate(new Date())
//				.type(MessageType.SERVER)
//				.groupId("63f74a7cfc73ec6683c2d227")
//				.build();
//		Message message4 = Message.builder()
//				.senderId("637b48a0edfbaa57cea2d696")
//				.content("Hello from message4")
//				.createdDate(new Date())
//				.type(MessageType.SERVER)
//				.groupId("63f74a7cfc73ec6683c2d227")
//				.build();
//		Message message5 = Message.builder()
//				.senderId("637b48a0edfbaa57cea2d696")
//				.content("Hello from message5")
//				.createdDate(new Date())
//				.type(MessageType.SERVER)
//				.groupId("63f74a7cfc73ec6683c2d227")
//				.build();
//		messageRepository.saveAll(Arrays.asList(message1, message2, message3, message4, message5));

//		Task task1 = Task.builder()
//				.title("Task 1")
//				.description("Mo ta task 1")
//				.deadline(new Date())
//				.assignerId("637b48a0edfbaa57cea2d696")
//				.assigneeIds(new ArrayList<>())
//				.parentTask("")
//				.groupId("63f74a7cfc73ec6683c2d227")
//				.build();
//		Task task2 = Task.builder()
//				.title("Task 2")
//				.description("Mo ta task 2")
//				.deadline(new Date())
//				.assignerId("637b48a0edfbaa57cea2d696")
//				.assigneeIds(new ArrayList<>())
//				.parentTask("")
//				.groupId("63fe157a08debb3763bcf59a")
//				.build();
//		Task task3 = Task.builder()
//				.title("Task 3")
//				.description("Mo ta task 3")
//				.deadline(new Date())
//				.assignerId("63e3737e135750675eb9950a")
//				.assigneeIds(new ArrayList<>())
//				.parentTask("")
//				.groupId("63fe164d08debb3763bcf59b")
//				.build();
//		Task task4 = Task.builder()
//				.title("Task 4")
//				.description("Mo ta task 4")
//				.deadline(new Date())
//				.assignerId("63e3737e135750675eb9950a")
//				.assigneeIds(Arrays.asList(Task.Assignee.builder().userId("637b48a0edfbaa57cea2d696").build()))
//				.parentTask("")
//				.groupId("63fe164d08debb3763bcf59b")
//				.build();
//		Task task5 = Task.builder()
//				.title("Task 5")
//				.description("Mo ta task 5")
//				.deadline(new Date())
//				.assignerId("63e3737e135750675eb9950a")
//				.assigneeIds(Arrays.asList(Task.Assignee.builder().userId("637b48a0edfbaa57cea2d696").build()))
//				.parentTask("")
//				.groupId("63fe166d08debb3763bcf59c")
//				.build();
//		Task task6 = Task.builder()
//				.title("Task 6")
//				.description("Mo ta task 6")
//				.deadline(new Date())
//				.assignerId("63e3737e135750675eb9950a")
//				.assigneeIds(Arrays.asList(Task.Assignee.builder().userId("637b48a0edfbaa57cea2d696").build()))
//				.parentTask("")
//				.groupId("63fe166d08debb3763bcf59c")
//				.build();
//		taskRepository.saveAll(Arrays.asList(task1, task2, task3, task4, task5, task6));
	}
}
