package liaison.groble.domain.gig.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @OneToMany(mappedBy = "parent")
  private List<Category> children = new ArrayList<>();

  private int depth; // 깊이 정보 (루트=0, 대분류=1, 소분류=2...)

  private String code; // 계층 구조를 표현하는 코드 (예: "001", "001001")

  private int displayOrder; // 같은 레벨 내에서의 정렬 순서

  private boolean isActive; // 활성화 여부
}
