package com.winet.ecommerce.util;

import com.winet.ecommerce.model.Cart;
import com.winet.ecommerce.payload.dto.CartDTO;
import com.winet.ecommerce.payload.dto.ProductDTO;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DtoUtils {

	private final ModelMapper modelMapper;

	public DtoUtils(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public CartDTO convertToDTO(Cart cart) {
		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

		Map<String, Object> productsDTOMap = new HashMap<>();

		cart.getCartItems().forEach(ci -> {
			ProductDTO productDTO = modelMapper.map(ci.getProduct(), ProductDTO.class);
			productsDTOMap.put("product", productDTO);
			productsDTOMap.put("quantity", ci.getQuantity());
		});

		cartDTO.setProducts(productsDTOMap);

		return cartDTO;
	}

}
