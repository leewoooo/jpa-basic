연관관계 매핑
===

## Goal

 - **객체와 테이블 연관관계의 차이를 이해**
 - **객체의 참조와 테이블의 외래 키를 매핑**

## 용어 이해

- 방향: 단방향, 양방향
- 다중성: 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M)
- **연관관계의 주인**: 객체 양방향 연관관계는 관리가 필요.

## 연관관계

객체는 참조를 통해 다른 객체와 연관 관계를 맺게 되지만 테이블은 FK(외래 키)를 이용하여 다른 테이블과 관계를 맺게 된다.

객체를 테이블 설계에 맞춰서 설계를 하게 되면 **객체와 객체 관계를 id를 통해 맺게 된다.** 이렇게 객체가 설계되면 관계를 맺고 있는 객체를 한번 더 Id값으로 조회해야하는 상황이 발생한다.

즉 **객체를 테이블에 맞추어 데이터 중심으로 설계를 하면 협력관계를 맺을 수 없게 된다.** 객체와 객체의 연관관계를 설정해 줄 때는 아래와 같은 예제처럼 설정해준다.

```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String memberName;

    //xToy에서 현재 class가 x가 되고 연관관계를 맺을 객체가 y가 된다.
    //member는 여러 명이고 여려명이 속한 팀은 하나이기 떄문에 Many(member) To One(Team)이 되는 것이다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Team team; // team의 Id를 참조하는 것이 아닌 team을 참조 함으로 연관관계를 맺게 해준다.
}

//...

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String teamName;
}
```

현 상태에서는 Member를 통해 Team을 조회할 수 있지만 **Team에서 Member를 조회할 없다.**(참조가 없다.)

## 양방향 연관관계와 연관관계의 주인

단방향에서 양방향으로 변경을 하여도 **테이블 구조는 변경할 필요가 없다**

Why? 객체와 테이블의 페러다임의 차이에서 오는 것인데 테이블은 FK만 있다면 **부모 테이블과 자식테이블이 양뱡향으로 연결이 되어있다.**

하지만 객체는 테이블과 달리 **참조를 이용한다.** 그럼 위의 예제를 양방향이 될 수 있도록 변경해보자.

```java
//...

@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String teamName;

    // mappedBy가 중요하다. (객체의 참조에서 반대편에 어떠한 것으로 mapping이 되어있는지 명시해주는 것이다.)
    // 관례상 빈 컬렉션으로 초기화 시켜준다.
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}
```

계속해서 반복적으로 이야기하지만 객체의 연관관계는 **참조**를 통해 맺어진다. 

객체에서 단방향을 구성하려면 아래와 같이 단방향이 2개로 이루어진 것을 양방향이라고 부르고 있는 것이다. (테이블은 **FK를 통해 두 테이블의 연관관계를 관리한다.**)
```
A -> B (단방향)

B <- A (단방향)
```

그렇다면 연관 관계를 어느쪽에서 관리를 할 것인가? 연관관계의 주인을 정해야한다.

### 양방향 매핑 규칙

- 객체의 두 관계중 하나를 연관관계의 주인으로 지정
- **연관관계의 주인만 외래키를 관리한다. (등록, 수정)**
- 주인이 아니라면 **읽기만 가능해야한다.**
- 주인은 `mappedBy` 속성을 사용하지 않는다. 주인이 아니면 `mappedBy` 속성을 사용한다.


### 누구를 주인으로 해야할까?

**FK(외래 키)를 가지고 있는 객체가 연관관계의 주인이 된다.** 즉 외래키를 가지고 있는 객체만 이용하여 **등록, 수정**이 가능하며
반대쪽은 **읽기 전용**만 가능하다.

**DB로 따지면 `N`이 되는 쪽이 연관관계의 주인**이 된다. 연관관계의 주인이 되었다고 비즈니스 적으로 중요도를 가지는게 아닐 수 있다.

### 양방향 연관관계 주의

- **순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자**
- 연관관계 편의 메소드를 생성하자 (즉 연관관계 주인이 생성되는 부분에 반대편의 객체에게 자기 자신을 추가해주거나 혹은 연관관계 편의 메소드를 생성하여 사용하자.)
    ```java
    // 생성자에서 추가
    public Member(Team team){
        this.team = team;
        team.getMember().add(team)
    }
  
    // 연관 관계 편의 메소드
    public void changeTeam(Team team){
        this.team = team;
        team.getMember().add(team)
    } 
    ```
- 연관관계 편의 메소드는 양쪽 다 존재하는 것이 아닌 한쪽에서만 만들어 사용하자.
- 양방향 매핑시에 무한루프를 조심하자.(toString() -> 만들지 말거나 관계가 있는 것은 빼고 사용, lombok, JSON 생성 라이브러리(**Entity를 Controller에서 바로 반환하지 마세요.**))

## 정리

**단방향 매핑만으로도 이미 연관관계 매핑은 완료** -> 처음에는 단방향으로 설계를 끝내고 이 후 필요하면 양방향 연관관계를 만들어 준다.

단방향 매핑을 잘 해두면 양방향 매핑을 추가하는 일은 쉽다!