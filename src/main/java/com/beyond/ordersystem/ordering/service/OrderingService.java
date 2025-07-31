package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderCreateDto;
import com.beyond.ordersystem.ordering.dto.OrderDetailDto;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.repository.OrderingDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderingRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderingDetailRepository orderingDetailRepository;

    // 주문 생성
    public Long createOrdering(List<OrderCreateDto> dtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        // 주문 먼저 저장하고
        Ordering ordering = Ordering.builder().orderStatus(OrderStatus.ORDERED).member(member).build();
        orderingRepository.save(ordering);

        // 저장한 주문번호 가져와서, OrderingDetail 저장
        // product id 뽑아서 product 객체 추출
        for(OrderCreateDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
            int quantity = dto.getProductCount();
            OrderDetail orderDetail = OrderDetail.builder().product(product).quantity(quantity).ordering(ordering).build();

            // @OneToMany + Cascade 조합으로 따로 save 없이 저장될 수 있게
            ordering.getOrderDetailList().add(orderDetail);

            // 재고 관리
            boolean check = product.decreaseQuantity(quantity); // 조건은 여기서 해결하는게 나을 듯
            if(!check) {
                // 모든 임시 저장 사항들을 롤백 처리
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
        }

        return ordering.getId();
    }

    // 주문 목록 조회
    public List<OrderListResDto> getOrderingList() {
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        List<Ordering> orderingList = orderingRepository.findAll();

        // Ordering (id, orderStatus), Member(memberEmail)
        // OrderDetail (detailId, productName, productCount)
        for(Ordering ordering : orderingList) {
            List<OrderDetailDto> orderDetailDtoList = new ArrayList<>();

            List<OrderDetail> orderDetailList = orderingDetailRepository.findByOrdering(ordering);
            for(OrderDetail orderDetail : orderDetailList) {
                orderDetailDtoList.add(OrderDetailDto.fromEntity(orderDetail));
            }
            OrderListResDto orderListResDto = OrderListResDto.fromEntity(ordering, orderDetailDtoList);
            orderListResDtoList.add(orderListResDto);
        }

        return orderListResDtoList;
    }
}
