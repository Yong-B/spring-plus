# CH 5 플러스 Spring 과제

Spring Boot 기반의 할 일 관리(Todo) 서비스입니다. 회원가입/로그인부터 할 일 생성, 담당자(Manager) 배정, 댓글, 관리자 권한 분리까지의 기능을 제공하며, JPA 실무 이슈 해결과 Spring Security 전환, QueryDSL을 활용한 쿼리 최적화에 중점을 두고 진행했습니다.

**기술 스택**: Java 17 · Spring Boot 3.3.3 · Spring Data JPA · QueryDSL · Spring Security · MySQL · JWT(jjwt)

**주요 도메인**: User · Todo · Manager · Comment · Log

---

## 목차

- [1. JPA & 트랜잭션](#1-jpa--트랜잭션)
- [2. 인증 & 인가](#2-인증--인가)
- [3. 테스트 & AOP](#3-테스트--aop)
- [4. QueryDSL](#4-querydsl)
- [트러블슈팅](#트러블슈팅)

---

## 1. JPA & 트랜잭션

| 항목 | 내용 |
|---|---|
| 읽기 전용 트랜잭션 이슈 | 클래스 레벨 `@Transactional(readOnly = true)` 하에서, 쓰기가 필요한 메소드에 `@Transactional`을 오버라이드하여 저장 기능 정상화 |
| 동적 검색 조건 | weather, 수정일 기간 등 있을 수도 없을 수도 있는 조건을, JPQL의 `:param IS NULL OR ...` 패턴으로 하나의 쿼리에서 처리 |
| Cascade를 통한 담당자 자동 등록 | 할 일 생성 시 생성자가 자동으로 담당자에 등록되도록, Todo-Manager 연관관계에 `cascade = CascadeType.PERSIST` 적용 |
| N+1 문제 해결 | 댓글 조회 시 연관 User를 꺼내 쓸 때마다 추가 쿼리가 나가던 문제를 `JOIN FETCH`로 해결. 테스트에서 영속성 컨텍스트를 초기화한 뒤 SQL 로그로 해결 여부 검증 |
| Transaction 전파 옵션 활용 | 매니저 등록 요청 로그를 남기는 `log` 테이블 추가. 로그 저장 메소드에 `@Transactional(propagation = REQUIRES_NEW)`를 적용해, 매니저 등록이 실패해 롤백되어도 로그는 독립적으로 저장되도록 구현 |

## 2. 인증 & 인가

| 항목 | 내용 |
|---|---|
| JWT에 닉네임 포함 | User에 nickname 컬럼 추가 후, 회원가입 → 토큰 발급 → 필터의 클레임 추출 → AuthUser 조립까지 전 구간에 반영 |
| Spring Security 전환 | 기존 서블릿 Filter + ArgumentResolver 조합을 Spring Security로 전환. JWT 검증은 `OncePerRequestFilter` 상속 필터로, 인가(권한 체크)는 `SecurityFilterChain`의 `authorizeHttpRequests`로 분리. 토큰 기반 인증 방식은 그대로 유지 |

## 3. 테스트 & AOP

| 항목 | 내용 |
|---|---|
| 컨트롤러 테스트 수정 | 예외 발생 시 실제 응답 상태 코드 및 본문 구조에 맞게 테스트 기대값 수정 |
| AOP 실행 시점 수정 | 관리자 권한 변경 API 실행 **전**에 로그가 남도록, 어드바이스를 `@After` → `@Before`로, 포인트컷 대상을 올바른 컨트롤러 메소드로 수정 |

## 4. QueryDSL

| 항목 | 내용 |
|---|---|
| 단건 조회 전환 | 기존 JPQL 단건 조회 쿼리를 QueryDSL로 전환. Custom 인터페이스 + Impl 구현체 패턴 적용, fetch join으로 N+1도 함께 방지 |
| 일정 검색 기능 (신규 API) | 제목 부분 일치, 생성일 범위, 담당자 닉네임 부분 일치 조건 검색 지원. `Projections.constructor`로 제목·담당자 수·댓글 수만 담은 DTO로 바로 매핑, 페이징 처리 |

---

## 트러블슈팅

작업 중 겪었던 문제와 해결 과정을 별도로 정리했습니다.

- [트러블슈팅 문서 링크](https://atom700.tistory.com/entry/QueryDSL-%EB%8F%84%EC%9E%85-%EA%B3%BC%EC%A0%95%EC%97%90%EC%84%9C-%EA%B2%BD%ED%97%98%ED%95%9C-N1-%EB%AC%B8%EC%A0%9C%EC%99%80-%EB%8F%99%EC%A0%81-%EA%B2%80%EC%83%89-%EC%BF%BC%EB%A6%AC-%EC%B5%9C%EC%A0%81%ED%99%94)
