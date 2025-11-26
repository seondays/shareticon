# 🎁 Shareticon

## 프로젝트 소개

쉐어티콘은 가족이나 친구들과 카카오톡을 이용하여 기프티콘을 공유하며 겪었던 불편함을 인식하고, `어떻게 하면 좀 더 편리하게 기프티콘을 공유할 수 있을까?` 하는 생각에서 탄생한 서비스입니다.

기본적인 기능 개발 및 배포를 완료하여 현재 실제로 운영 및 직접 서비스를 사용 중입니다. 사용자 피드백을 바탕으로 지속적인 기능 추가 개선 및 유지보수를 진행하고 있습니다.

- 개발 기간 : 2025. 04 ~ 운영 중
- 개발 인원 : 1인 (백엔드 담당 / 프론트 Cursor AI 사용한 바이브 코딩을 통해 구현)

</br>

## Link
- [🔗 운영 서비스](https://www.shareticon.site)
- [🔗 API 문서](https://api.shareticon.site/docs)
- [🔗 프로젝트 개발 일지](https://shareticon.notion.site/?v=1b27d38e379c811fbc78000c3606d203)
</br>

## 기술 스택
### Backend

- **Core** : <img src="https://img.shields.io/badge/Java-007396?style=&logo=OpenJDK&logoColor=white"> <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=&logo=Spring%20Boot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=&logo=Spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring Batch-6DB33F?style=&logo=Spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring Retry-6DB33F?style=&logo=Spring&logoColor=white">

- **Database** : <img src="https://img.shields.io/badge/MySQL-4479A1?style=&logo=MySQL&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=&logo=Redis&logoColor=white"> <img src="https://img.shields.io/badge/H2-09476B?style=&logo=h2database&logoColor=white">

- **Security** : <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=&logo=Spring%20Security&logoColor=white"> <img src="https://img.shields.io/badge/OAuth2-3C7EBB?style=&logo=OAuth&logoColor=white">

- **Infrastructure** : <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=&logo=Amazon%20EC2&logoColor=white"> <img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=&logo=Amazon%20S3&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=&logo=Docker&logoColor=white"> <img src="https://img.shields.io/badge/nginx-%23009639.svg?style=&logo=nginx&logoColor=white">

- **Documentation** : <img src="https://img.shields.io/badge/asciidoctor-E40046?style=&logo=asciidoctor&logoColor=white"> <img src="https://img.shields.io/badge/Spring_REST_Docs-4CAF50?style=flat&logo=&logoColor=white">

- **Test** : <img src="https://img.shields.io/badge/JUnit5-25A162?style=&logo=JUnit5&logoColor=white"> <img src="https://img.shields.io/badge/Mockito-FF9800?style=flat&logo=&logoColor=white">

</br>

## 주요 구현 기능
- Junit5를 이용하여 계층 별 전략을 적용한 테스트 코드 작성 및 환경 최적화
- 제한된 예산 내에서 AWS 서비스를 이용하여 최선의 서버 아키텍처 설계
- Resource Server 활성화를 통한 안정성 높은 OAuth2 로그인 기능 구현
- 정합성과 안정성을 고려한 이중 저장소(DB-S3) 기반의 쿠폰 API 설계 [(관련 기록 🔗)](https://shareticon.notion.site/DB-S3-2227d38e379c808da0cbd048f6e18be9) 
- QueryDSL을 활용하여 동적 쿼리 구현, 쿠폰 조회 시 필터링 기능 개선 [(관련 기록 🔗)](https://shareticon.notion.site/QueryDsl-2747d38e379c80989275e1c930d8025a) 
- Spring Batch로 만료 쿠폰 자동 상태 업데이트 처리 [(관련 기록 🔗)](https://shareticon.notion.site/2467d38e379c80b48873e933c0a3fcd1)
- Prometheus & Grafana 기반의 서버 모니터링 체계 구축
- Caffeine 로컬 캐시 도입을 통한 쿠폰 조회 응답 속도 37% 개선 [(관련 기록 🔗)](https://shareticon.notion.site/Presigned-URL-Caffeine-27e7d38e379c80fcb96cca61edeb3bb6)
- CloudWatch 기반의 EC2 인스턴스 자동 복구 시스템 구축 [(관련 기록 🔗)](https://seondays.tistory.com/89)

</br>

## 아키텍처
실제로 서비스를 배포 후 운영을 해야 하는 상황에서 비용 문제를 고려하지 않을 수 없어, 제한된 예산 내에서 최대한 효율적으로 아키텍처를 설계하고자 했습니다.

- 예상 MAU가 10명 이하로 매우 적을 것으로 예측되었고 이를 바탕으로 서버의 가용성, 보안성과 비용 사이의 트레이드 오프에서 비용에 높은 비중을 두어 리소스를 최소화했습니다.
  - 단일 AZ 사용, 최소 인스턴스(2대), 최소 DB
- AZ 분산의 부재로 인해 현재 모든 인스턴스들이 단일 장애 지점임을 인식, 백엔드 애플리케이션 내부 상태 확인을 위해 모니터링 시스템을 구축했습니다.
  - 서버와 분리된 외부 퍼블릭 인스턴스에 배치하여 서버에 문제가 발생했을 때도 정상적으로 해당 사항을 파악할 수 있도록 설계
- 이후 필요에 따라 서비스 확장에 대비할 수 있도록 기초 아키텍처 기반은 유지하고 있습니다.
  - 서브넷 분리하여 두번째 AZ 네트워크 설정 유지

<img width="1331" height="1044" alt="백엔드" src="https://github.com/user-attachments/assets/91676c29-28d7-4ece-89a0-11d6ad834c31" />

</br>

## ERD
<img width="1391" height="1032" alt="Image" src="https://github.com/user-attachments/assets/40ef3d15-9057-40be-8695-f3462bfe3d37" />

</br>

## 주요 화면 소개

### 쿠폰 등록과 삭제 화면

|                                                          쿠폰 등록                                                              |                                                           쿠폰 삭제                                                        |
| :---------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/02e6b5f4-6b44-4891-a1da-ee1f6953778e" alt="register-coupon" width="350"> | <img src="https://github.com/user-attachments/assets/0207a324-e7ff-4eba-a684-9960e6073517" alt="del-coupon" width="350"> |

### 쿠폰 목록 화면

|                                                    쿠폰 사용완료 처리                                                    |                                              쿠폰 필터링                                              |
| :----------------------------------------------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/389eb10c-abd5-4fe4-b78b-c50c21f62a62" alt="used-coupon" width="350"> | <img src="https://github.com/user-attachments/assets/55d188ba-338b-4d6a-b92b-b5e59e5101a5" alt="filtering-coupon" width="350"> |

### 그룹 생성
<img src="https://github.com/user-attachments/assets/abc8603f-3890-4bf9-8c6d-098cfab43d39" alt="create-group" width="350">

### 그룹 가입 화면

|                                                          그룹 가입하기                                                              |                                                           그룹 가입 수락하기                                             |
| :---------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/7f61bae7-75f0-4c17-8556-17dc6b40e09a" alt="join-group" width="350"> | <img src="https://github.com/user-attachments/assets/b9bd837d-6481-4d99-8adf-4032ada87e4b" alt="join-group-accept" width="350"> |

### 그룹 별칭 변경 화면
<img src="https://github.com/user-attachments/assets/271fe489-5fd0-4839-a3ff-51753da570ff" alt="group-alias" width="350">

### 쿠폰 찜하기 화면
<img src="https://github.com/user-attachments/assets/5536b705-d9ce-48ec-9b66-af068c4f5550" alt="join-group" width="350">

