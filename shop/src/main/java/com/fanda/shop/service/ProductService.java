package com.fanda.shop.service;

import com.fanda.shop.dto.ProductDetailResponseDto;
import com.fanda.shop.dto.ReviewImageDto;
import com.fanda.shop.dto.ReviewResponseDto;
import com.fanda.shop.dto.UserResponseDto;
import com.fanda.shop.entity.Product;
import com.fanda.shop.entity.Review;
import com.fanda.shop.entity.ReviewImage;
import com.fanda.shop.repository.ProductRepository;
import com.fanda.shop.repository.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserClient userClient;
    private final ProductRepository productRepository;

    public ProductDetailResponseDto getProductDetail(Long productId){
        Product product = productRepository.findById(productId).orElseThrow(()-> new NoSuchElementException("상품을 찾을 수 없습니다. id : "+ productId));

        List<ReviewResponseDto> reviewResponseDtoList = new ArrayList<>();

        for(Review review : product.getReviews()){
            UserResponseDto userResponseDto = userClient.getUser(review.getUserId());
            List<ReviewImageDto> imageDtoList = new ArrayList<>();
            for(ReviewImage image : review.getImages()){
                imageDtoList.add(new ReviewImageDto(image.getImageUrl()));
            }

            ReviewResponseDto reviewResponseDto = new ReviewResponseDto(
                    review.getUserId(),
                    userResponseDto.nickname(),
                    review.getRating(),
                    review.getContent(),
                    review.getCreatedAt(),
                    imageDtoList
            );

            reviewResponseDtoList.add(reviewResponseDto);
        }

        return new ProductDetailResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getAverageRating(),
                reviewResponseDtoList
        );
    }
}
