package jpabasic.jpabasic.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
