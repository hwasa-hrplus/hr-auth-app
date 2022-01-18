package com.poscoict.auth.recvmodules;

import java.util.HashSet;
import java.util.Set;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poscoict.auth.models.ERole;
import com.poscoict.auth.models.Role;
import com.poscoict.auth.models.User;
import com.poscoict.auth.payload.request.SignupRequest;
import com.poscoict.auth.payload.response.MessageResponse;
import com.poscoict.auth.repository.RoleRepository;
import com.poscoict.auth.repository.UserRepository;
import com.poscoict.auth.security.jwt.JwtUtils;

@Component
class RecvModule {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;
	
	@RabbitListener(bindings = @QueueBinding(
			exchange = @Exchange(
					name = "Exchange", type = ExchangeTypes.TOPIC), 
			value = @Queue(name = "bananaSignup"), // 받는놈의 키																																					// 키
			key = "signup")) // 주는놈의 키
	
	public void receiver(String signUpJson) throws JsonMappingException, JsonProcessingException {
	    ObjectMapper objectMapper = new ObjectMapper();
		
		SignupRequest signUpRequest = objectMapper.readValue(signUpJson, SignupRequest.class);
		
		System.out.println(signUpRequest.toString());			

		// Create new user's account
		User user = new User(signUpRequest.getUsername(),
							 encoder.encode(signUpRequest.getPassword()),
							 signUpRequest.getKorName(),
							 signUpRequest.getId());

		Set<String> strRoles = signUpRequest.getRole();
		System.out.println("str:"+strRoles.toString());
		Set<Role> roles = new HashSet<>();		
		
		strRoles.forEach(role -> {
			switch (role) {
			case "admin":
				Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				roles.add(adminRole);
				break;	
				
			default:
				System.out.println(ERole.ROLE_USER);
				Role userRole = roleRepository.findByName(ERole.ROLE_USER)
						.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
				
				roles.add(userRole);
			}
		});		

		user.setRoles(roles);
		userRepository.save(user);		
	}
}