package hello.springdb2.v2;

import org.springframework.data.jpa.repository.JpaRepository;

import hello.springdb2.domain.Item;

public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {

}
