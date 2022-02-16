JPA 소개 및 기본
===

- JPA는 어떠한 Database에 종속되면 안된다.


- Database마다 다른 Syntax(이하 방언)들을 JPA가 번역을 하여 각 Database의 방언에 맞게 변경하여 사용해준다.


- JPA의 데이터 변경은 모두 **트랜잭션 안에서 이루어 져야 한다.**


- JPA를 이용할 때 where절과 같이 조건을 부여하여 조작을 하고 싶다면 **JPQL**을 사용하면 된다.
(RDBMS는 query의 대상이 테이블이지만 JPQL은 query의 대상이 **객체**이다.)


- 검색 query를 사용할 때 **물리적인 테이블을 대상으로 query를 하게 되면 Database에 종속적일 수 밖에 없기 떄문에** JPQL을 이용하여 Database에 종속되지 않고 조건을 부여하여 검색할 수 있다.(JPQL = 객체 지향 SQL)





