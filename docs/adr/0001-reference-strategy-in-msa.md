# [ADR-0001] MSA 환경에서의 타 도메인 데이터 참조 및 조합 전략

* **상태**: 승인됨 (Accepted) [2025-12-09]
* **결정자**: Social Service Team
* **관련 문서**: User Service API, Pet Service API 명세

## 1. 배경 및 문제 (Context)
우리는 현재 모놀리식 구조가 아닌 MSA(Microservices Architecture)를 기반으로 서비스를 구축하고 있다.
Social Service의 핵심 도메인인 `Feed`는 화면에 표시될 때 작성자의 정보(닉네임, 프로필)와 반려동물 정보(이름, 품종)가 필요하다.
하지만 해당 데이터의 원천(Source of Truth)은 각각 `User Service`와 `Pet Service`에 존재하며, 각 서비스는 물리적으로 분리된 데이터베이스를 사용한다.
따라서 기존의 JPA 연관관계(`@ManyToOne`, `@OneToOne`)를 사용하여 객체를 직접 참조하거나 테이블을 조인(Join)하는 것이 불가능하다.

## 2. 결정 사항 (Decision)
우리는 타 도메인의 데이터를 참조할 때 **ID 참조(Indirect Reference) 방식**을 사용하며, 데이터 조합(Aggregation)은 **API Composition(Feign Client) 패턴**을 사용하기로 결정했다.

구체적인 구현 전략은 다음과 같다:
1. **저장**: `Feed` 엔티티에는 `User` 객체가 아닌 `userId`, `petId` (Long 타입)만