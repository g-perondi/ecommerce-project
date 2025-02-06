package com.winet.ecommerce.util;

import com.winet.ecommerce.model.Cart;
import com.winet.ecommerce.model.Order;
import com.winet.ecommerce.payload.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DtoUtils {

	private final ModelMapper modelMapper;

	public DtoUtils(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public CartDTO convertCartToDTO(Cart cart) {
		CartDTO cartDTO = new CartDTO();

		List<CartItemDTO> cartItemsDTO = cart.getCartItems().stream()
				.map(ci -> {
					CartItemDTO ciDTO = new CartItemDTO();
					ciDTO.setCartItemId(ci.getCartItemId());
					ciDTO.setProduct(modelMapper.map(ci.getProduct(), ProductDTO.class));
					ciDTO.setQuantity(ci.getQuantity());
					ciDTO.setProductPrice(ci.getProductPrice());
					ciDTO.setDiscount(ci.getDiscount());
					return ciDTO;
				})
				.toList();

		cartDTO.setCartId(cart.getCartId());
		cartDTO.setTotalPrice(cart.getTotalPrice());
		cartDTO.setCartItems(cartItemsDTO);

		return cartDTO;
	}

	public OrderDTO convertOrderToDTO(Order order) {

		OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
		orderDTO.setPayment(modelMapper.map(order.getPayment(), PaymentDTO.class));

		List<OrderItemDTO> orderItemsDTO = new ArrayList<>();

		order.getOrderItems().forEach(item -> {
			OrderItemDTO orderItemDTO = modelMapper.map(item, OrderItemDTO.class);
			orderItemDTO.setProduct(modelMapper.map(item.getProduct(), ProductDTO.class));
			orderItemDTO.setQuantity(item.getQuantity());
			orderItemsDTO.add(orderItemDTO);
		});

		orderDTO.setOrderItems(orderItemsDTO);

		return orderDTO;
	}

}
