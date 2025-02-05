package com.winet.ecommerce.repository;

import com.winet.ecommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

	@Modifying
	@Query("DELETE FROM Cart c WHERE c.user.userId = ?1 ")
	void deleteForUser(Long userId);

	@Query("SELECT c FROM Cart c WHERE c.user.email = ?1")
	Optional<Cart> findByEmail(String email);

	@Query("SELECT c FROM Cart c WHERE c.user.userId = ?1")
	Optional<Cart> findByUserId(Long userId);

}
