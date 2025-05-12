package com.example.AssetTrading.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transaction_idx;

    /** ManyToOne은 FetchType.LAZY 쓰면 좋다는데 일단 보류 뭔말인지 몰라서
        JoinColumn과 Column은 외래키 인가, 아닌가의 차이 (User, SellProduct는 다른 곳에서 가져온 것이라서 그럼) **/
    // 구매자
    @ManyToOne
    @JoinColumn(name = "buyer_user_idx")
    private User buyer;

    // 판매자
    @ManyToOne
    @JoinColumn(name = "seller_user_idx")
    private User seller;

    // 거래 할 상품
    @ManyToOne
    @JoinColumn(name = "product_idx")
    private SellProduct sellProduct;

    /** 의문점인것들 **/
    /** Transition에도 거래 금액이 필요한가? private int price 를 해야되는가? **/

    // String으로 해야 유지보수와 가독성이 좋음
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    public TransactionStatus status;

    // 거래 생성 시간
    @Column(name = "created_at")
    private LocalDateTime createdTime;

    // 거래 승인 시간
    @Column(name = "processed_at")
    private LocalDateTime processedTime;

    // 거래 수정이 필요한가? 일단 보류
    // 거래 수정 시간
    @Column(name = "updated_at")
    private LocalDateTime updatedTime;
    // 거래 생성시 사용 코드 updatedTime = LocalDateTime.now();

    // 거래 완료 시간
    @Column(name = "successed_at")
    private LocalDateTime successTime;

    // 거래 삭제 시간
    @Column(name = "deleted_at")
    private LocalDateTime deletedTime;
}
