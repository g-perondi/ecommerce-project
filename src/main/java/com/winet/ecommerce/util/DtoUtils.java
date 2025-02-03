package com.winet.ecommerce.util;

import com.winet.ecommerce.model.Cart;
import com.winet.ecommerce.payload.dto.CartDTO;
import com.winet.ecommerce.payload.dto.ProductDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtoUtils {

	private final ModelMapper modelMapper;

	public DtoUtils(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public CartDTO convertToDTO(Cart cart) {
		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		List<ProductDTO> productsDTOs = cart.getCartItems().stream()
				.map(ci -> {
					return modelMapper.map(ci.getProduct(), ProductDTO.class);
				})
				.toList();
		cartDTO.setProducts(productsDTOs);

		return cartDTO;
	}

}
