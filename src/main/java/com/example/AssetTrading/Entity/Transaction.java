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

    private int product_idx;

    private Long id;

    // ManyToOne은 FetchType.LAZY 쓰면 좋다는데 일단 보류

    // 구매자
    @ManyToOne
    @JoinColumn(name = "buyer_user_id")
    private User buyer;

    // 판매자
    @ManyToOne
    @JoinColumn(name = "seller_user_id")
    private User seller;

    // 거래할 상품
    @ManyToOne
    @JoinColumn(name = "SellProduct")
    private SellProduct sellProduct;

    /** 의문점인것들 **/
    /** Transition에도 거래 금액
     private int price 를 해야되는가? **/

    // String으로 해야 유지보수와 가독성이 좋음
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    public TransactionStatus status;

    // 거래 시작 시간
    private LocalDateTime startTime;
    // 거래 생성시 사용 코드 startTime = LocalDateTime.now();

    // 거래 완료 시간
    private LocalDateTime finishTime;

    @Builder
    public Transaction(User buyerId, User sellerId, SellProduct sellProduct, TransactionStatus status, LocalDateTime startTime) {
        this.buyer = buyerId;
        this.seller = sellerId;
        this.sellProduct = sellProduct;
        this.status = status;
        this.startTime = startTime;

    }
}
