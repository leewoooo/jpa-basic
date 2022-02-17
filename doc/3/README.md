엔티티 매핑
===

## 엔티티 매핑 소개

- 객체와 테이블 매핑: @Entity, @Table
- 필드와 컬럼 매핑: @Column
- 기본 키 매핑: @Id
- 연관관계 매핑: @ManyToOne, @JoinColumn, ...

## 객체와 테이블 매핑

`@Entity`가 붙은 class는 **JPA가 관리하는 엔티티이다.**

JPA를 사용해서 테이블과 매핑할 클래스는 `@Entity` 필수

### 주의

- **기본 생성자 필수**(접근 제어자를 public, protected까지 허용)
- final class, enum, interface, inner 클래스는 사용 X
- column값으로 사용할 필드는 **final 사용 불가**

## @Table

class 위에 `@Table`이라는 애노테이션을 사용하여 추가적으로 option을 지정할 수 있다. 

- name: 매핑할 테이블 이름
- catalog: 데이터베이스 catalog 매핑
- schema: 데이터 베이스 schema 매핑
- uniqueConstraints: 유니크 제약 조건

## 데이터베이스 스키마 자동 생성

애플리케이션 실행 시점에 엔티티를 확인하여 테이블을 생성해준다. **데이터 베이스 방언을 활용해서** 데이터베이스에 맞는 
적적한 DDL을 생성한다.

**중요** 생성된 DDL은 개발 서버에서만 사용하는 것을 지향한다.

option은 5가지가 있다.

1. create: 기존 테이블 삭제 후 다시 생성 (DROP + CREATE)
2. create-drop: create와 같으나 종료 시점에 테이블 DROP
3. update: 변경분만 반영(운영DB에는 사용하면 안됨, column이 추가되는 것만 감지하고 삭제되는 것은 update 되지 않는다.)
4. validate: 엔티티오 테이블 정상 매핑되어있는지만 확인
5. none: 사용하지 않음

### 주의점

**운영 장비에는 절대 create, create-drop, update를 사용하면 안된다.**
- 개발 초기 단계는 create, update를 사용하여 local에서 사용해도 된다.
- 테스트 서버는 update 또는 validate를 이용하면 된다. (개발과 운영의 중간에 있는 서버)
- 스테이징과 운영서버는 **validate, none**을 이용 (운영에서 update 상태에서 alter가 잘못 사용하게 되면 큰 장애가 일어날 수 있다.)

결론: local에서는 자유롭게 사용하고 테스트 서버나, 운영 서버에는 제약적으로 사용한다. (**DB 계정 권한을 분리하여 사용할 수 있게 한다.**)

## 매핑 어노테이션

1. @Column: 컬럼 정보 매핑
2. @Temporal: 날짜 타입 매핑
3. @Enumerated: enum 타입 매핑
4. @Lob: BLOB, CLOB 매핑
5. @Transient: 특정 필드를 컬럼에 추가하고 싶지 않을 때 사용 

## @Column

사용할 수 있는 option들은 다음과 같다.

1. name: 필드와 매핑할 테이블 컬럼 이름
2. insertable, updatable: 등록 변경 가능 여부
3. nullable: NOT NULL 제약조건
4. unique: column에 유니크 제약조건을 부여한다. (잘 사용하지 않는다. 제약조건 이름이 random하게 만들어진다. @Table 안에서 name과 같이 지정)
5. columnDefinition: column의 정보를 직접 부여할 수 있다.

## @Enumerated

EnumType에는 `ORDINAL, STRING`을 지원하는데 **`ORDINAL`는 사용하지 않는다.**

`ORDINAL`는 순서대로(index)값이 저장하게 되는데 **이 후 추가적으로 Enum에 값이 추가 될 경우 값이 꼬인다.**

```java
public enum EnumTest{
    A,B
}
```

이 상태로 저장하면  A -> 0, B -> 1 으로 저장이 된다. 이 후 `EnumTest`이 아래와 같이 변경되었다고 가정해보자.

```java
public enum EnumTest{
    A, C, B
}
```

이렇게 변경되면 A -> 0, C -> 1, B -> 2로 저장된다. 이렇게 되면 이전 B로 저장되어 있던 column들의 값이 조회하였을 때 C가 된다. 그렇기 때문에
**ORDINAL가 아니라 STRING으로 사용하여 문자열 그대로 저장하자.**

## @Temporal

java 8 환경에서는 `LocalDataTime`을 그대로 사용하면 된다.(TIMESTAMP로 지정된다.) `@Temporal`를 부여할 필요 없다.

## 기본 키 매핑

1. IDENTITY
2. SEQUENCE
3. TABLE
4. 직접 생성

### IDENTITY 전략

기본키 생성을 Databased에 위임하는 것이다. 즉 Database에서 기본 키를 생성되기 때문에 **Id**값을 알기 위해서는
Database에 들어갔다가 나와야 한다.

하지만 **영속성 컨텍스트에서 관리가 되려면 식별자(@Id)값이 있어야하는데 어떻게 해야할까?**

`IDENTITY`전략에서만 `persist`를 호출 한 시점에 바로 **Insert query**를 실행하게된다. 이 후 생성된  **`@Id`**값을 이용하여 **영속성 컨텍스트에서 관리**

그렇기 때문에 **지연 쓰기 전략을 사용할 수 는 없다.**

이 외 전략들은 `commit`시점에 insert query가 실행된다.

### 권장하는 식별자 전략

- **기본 키 제약 조건** : null이 아니여야 하며 **변하면 안된다.**
- 미래까지 이 조건을 만족하는 자연키(비즈니스 적으로 의미 있는 키)는 찾기 힘들다. 대리키(비즈니스와 상관 없는 키)를 사용하자.
- **권장: Long형 + 대체키 + 키 생성전략 사용**
