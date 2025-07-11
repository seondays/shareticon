# 🎁 Shareticon

## 프로젝트 소개

쉐어티콘은 가족이나 친구들과 카카오톡을 이용하여 기프티콘을 공유하며 겪었던 불편함을 인식하고, `어떻게 하면 좀 더 편리하게 기프티콘을 공유할 수 있을까?` 하는 생각에서 탄생한 서비스입니다.

기본적인 기능 개발 및 배포를 완료하여 현재 실제로 운영 및 직접 서비스를 사용 중입니다. 사용자 피드백을 바탕으로 지속적인 기능 추가 개선 및 유지보수를 진행하고 있습니다.

- 개발 기간 : 2025. 04 ~ 운영 중
- 개발 인원 : 1인 (백엔드 담당 / 프론트 Cursor AI 사용하여 구현)

## Link
- [🔗 운영 서비스](www.shareticon.site)
- [🔗 API 문서](api.shareticon.site/docs)

## 기술 스택
### Backend

- **Core** : <img src="https://img.shields.io/badge/Java-007396?style=&logo=OpenJDK&logoColor=white"> <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=&logo=Spring%20Boot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=&logo=Spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring Batch-6DB33F?style=&logo=Spring&logoColor=white"> <img src="https://img.shields.io/badge/Spring Retry-6DB33F?style=&logo=Spring&logoColor=white">

- **Database** : <img src="https://img.shields.io/badge/MySQL-4479A1?style=&logo=MySQL&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=&logo=Redis&logoColor=white"> <img src="https://img.shields.io/badge/H2-09476B?style=&logo=h2database&logoColor=white">

- **Security** : <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=&logo=Spring%20Security&logoColor=white"> <img src="https://img.shields.io/badge/OAuth2-3C7EBB?style=&logo=OAuth&logoColor=white">

- **Infrastructure** : <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=&logo=Amazon%20EC2&logoColor=white"> <img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=&logo=Amazon%20S3&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=&logo=Docker&logoColor=white"> <img src="https://img.shields.io/badge/nginx-%23009639.svg?style=&logo=nginx&logoColor=white">

- **Documentation** : <img src="https://img.shields.io/badge/asciidoctor-E40046?style=&logo=asciidoctor&logoColor=white"> <img src="https://img.shields.io/badge/Spring_REST_Docs-4CAF50?style=flat&logo=&logoColor=white">

- **Test** : <img src="https://img.shields.io/badge/JUnit5-25A162?style=&logo=JUnit5&logoColor=white"> <img src="https://img.shields.io/badge/Mockito-FF9800?style=flat&logo=&logoColor=white">

## 아키텍처
실제로 서비스를 배포 후 운영을 해야 하는 상황에서 비용 문제를 고려하지 않을 수 없어, 제한된 예산 내에서 최대한 효율적으로 아키텍처를 설계하고자 했습니다.

- 예상 MAU가 10명 이하로 매우 적을 것으로 예측되었고 이를 바탕으로 서버의 가용성, 보안성과 비용 사이의 트레이드 오프에서 비용에 높은 비중을 두어 리소스를 최소화했습니다.
  - 단일 AZ 사용, 최소 인스턴스(2대), 최소 DB
- 이후 필요에 따라 서비스 확장에 대비할 수 있도록 기초 아키텍처 기반은 유지하고 있습니다.
  - 두번째 AZ 네트워크 설정 유지

<img width="1315" height="1163" alt="Image" src="https://github.com/user-attachments/assets/a933957b-f8f0-4cb4-8bcd-70cce8083382" />

## ERD
<img width="1391" height="1032" alt="Image" src="https://github.com/user-attachments/assets/40ef3d15-9057-40be-8695-f3462bfe3d37" />

## 주요 화면 소개

### 쿠폰 등록과 삭제 화면

|                                                          쿠폰 등록                                                              |                                                           쿠폰 삭제                                                        |
| :---------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/02e6b5f4-6b44-4891-a1da-ee1f6953778e" alt="register-coupon" width="350"> | <img src="https://github.com/user-attachments/assets/0207a324-e7ff-4eba-a684-9960e6073517" alt="del-coupon" width="350"> |

### 쿠폰 목록 화면

|                                                    쿠폰 사용완료 처리                                                    |                                              쿠폰 필터링                                              |
| :----------------------------------------------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/389eb10c-abd5-4fe4-b78b-c50c21f62a62" alt="used-coupon" width="350"> | <img src="https://github.com/user-attachments/assets/da5d0614-1271-4748-8b91-e18cd61b1032" alt="filtering-coupon" width="350"> |

### 그룹 생성
<img src="https://github.com/user-attachments/assets/abc8603f-3890-4bf9-8c6d-098cfab43d39" alt="create-group" width="350">

### 그룹 가입 화면

|                                                          그룹 가입하기                                                              |                                                           그룹 가입 수락하기                                             |
| :---------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/7f61bae7-75f0-4c17-8556-17dc6b40e09a" alt="join-group" width="350"> | <img src="https://github.com/user-attachments/assets/b9bd837d-6481-4d99-8adf-4032ada87e4b" alt="join-group-accept" width="350"> |

### 그룹 별칭 변경 화면
<img src="https://github.com/user-attachments/assets/271fe489-5fd0-4839-a3ff-51753da570ff" alt="group-alias" width="350">

