package com.winet.ecommerce.repository;

import com.winet.ecommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

	@Query("SELECT c FROM Cart c WHERE c.user.email = ?1 AND c.cartId = ?2")
	Optional<Cart> findByUserEmailAndCartId(String email, Long cartId);

}
