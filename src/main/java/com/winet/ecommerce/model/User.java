package com.winet.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = "username"),
				@UniqueConstraint(columnNames = "email")
		}
)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@NotBlank
	@Size(max = 20)
	private String username;

	@NotBlank
	@Email
	@Size(max = 50)
	private String email;

	@NotBlank
	@NotNull
	private String role;

	@NotBlank
	private String password;

	@ToString.Exclude
	@OneToOne(
			mappedBy = "user",
			cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
			orphanRemoval = true
	)
	private Cart cart;

	@OneToMany(mappedBy = "user")
	private List<Order> orders;

	public User(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
	}

}
