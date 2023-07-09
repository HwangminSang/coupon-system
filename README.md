# coupon-system
< 해당 과정은 인프런의 강의를 참고하였습니다. >
- 카프카 , 자바 , jpa , 레디스 이용

## 요구사항 정의 
#### - 1차
 선착순 100명에게 할인쿠폰을 제공하는 이벤트를 필요로함

##### 조건
1. 선착순 100명에게만 지급되어야한다.
2. 101개 이상이 지급되어서는 안된다.
3. 순간적으로 몰리는 트래픽을 버틸수 있어야한다.

##### 해결방안

- 다수의 스레드가 하나의 자원에 접근하여 작업진행시 레이스 컨디션 발생 그에 따른 해결방법으로 레디스를 이용하여 순차적으로 진행 ( 레디스의 싱글스레드 이용) 
- 순각적으로 몰리는 트래픽을 버티기 위해 API 서버에서 바로 쿠폰 발급하여 디비에 INSERT를 하지 않고 카프카 (분산 이벤트 스트리밍) 을 이용하여 
디비에 저장하여 DB에 대한 부하를 줄임.

#### - 2차
 발급가능한 쿠폰개수를 1인당 1개로 제한하기

< DB연관되어 해결 >
1. coupon 타입을 엔티티에 추가 시켜 검사 방법
2. 해당 유저가 해당 쿠폰을 발급 받았는지 등록하기전 검사

 < 디비에 검색을하는 쿼리가 한번 더 나가기때문에 성능적으로 이슈가 있을것이고 현재 카프카를 이용하여 쿠폰 발급을 진행중이기 때문에 한 유저가 쿠폰 발급을 동시에 진행 하였을경우 컨슈머에서 토픽에 가져온 데이터를 INSERT 하기전 싱크 이슈가 있을것으로 예상 >
  
##### 해결방법


1. Set 자료구조를 이용 (동일한 값 저장 x)
2. 레디스에서도 해당 자료구조를 제공 (sadd key value 명령어 사용)
3. 레디스를 통해 해당 유저의 쿠폰 발급여부 확인 후 진행.


#### - 3차
시스템 개선하기 
- 쿠폰을 발급하다가 에러가 발생한다면 ? 
- EX) 토픽에서 가져온 데이터를 컨슈머가 db에 insert 할 시 실패 한다면 실질적으로 발급된 쿠폰의 갯수와 레디스에 저장된 쿠폰의 갯수가 같지 않는 현상 발생

##### 해결방법
 - 에러 로그와 백업데이트를 남기는 형식으로 진행하여 해결
 - 에러를 따로 남겨 어떤 사용자에게 어떤 이유로 발급되지 않았는지 확인 후 추후 배치프로그램으로 쿠폰 재생성.

