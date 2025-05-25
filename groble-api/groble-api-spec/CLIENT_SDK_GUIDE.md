# Next.js 클라이언트 SDK 자동 생성 가이드

## 개요
OpenAPI 명세서를 기반으로 TypeScript 클라이언트 SDK를 자동 생성하여 Next.js 프로젝트에서 사용하는 방법을 설명합니다.

## 장점

### 1. **타입 안정성**
- API 응답 타입이 자동으로 생성되어 TypeScript의 장점을 최대한 활용
- 컴파일 타임에 타입 오류 감지

### 2. **생산성 향상**
- API 엔드포인트 변경 시 자동으로 클라이언트 코드 업데이트
- 보일러플레이트 코드 작성 불필요

### 3. **일관성 유지**
- 백엔드 API와 프론트엔드 코드 간 일관성 보장
- API 문서와 실제 구현의 동기화

## 사용 방법

### 1. SDK 생성
```bash
# 단일 API SDK 생성
./gradlew :groble-api:groble-api-spec:generateTypeScriptClient

# 모든 클라이언트 SDK 생성
./gradlew :groble-api:groble-api-spec:generateAllClients

# Next.js 프로젝트로 복사
./gradlew :groble-api:groble-api-spec:copyToNextJs
```

### 2. Next.js 프로젝트 설정

#### 패키지 설치
```bash
npm install axios
# 또는
yarn add axios
```

#### API 클라이언트 설정
```typescript
// src/lib/api/client.ts
import { Configuration, NotificationApi } from './generated';

const configuration = new Configuration({
  basePath: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  accessToken: async () => {
    // 토큰 가져오기 로직
    const token = await getAuthToken();
    return token;
  },
});

export const notificationApi = new NotificationApi(configuration);
```

### 3. Next.js에서 사용

#### Pages Router 예시
```typescript
// pages/notifications.tsx
import { notificationApi } from '@/lib/api/client';
import { NotificationItems } from '@/lib/api/generated';

export default function NotificationsPage({ notifications }: { notifications: NotificationItems }) {
  return (
    <div>
      <h1>알림 ({notifications.unreadCount}개 읽지 않음)</h1>
      {notifications.notifications.map((item) => (
        <div key={item.notificationId}>
          <h3>{item.title}</h3>
          <p>{item.content}</p>
        </div>
      ))}
    </div>
  );
}

export async function getServerSideProps() {
  try {
    const response = await notificationApi.getNotifications();
    return {
      props: {
        notifications: response.data.data,
      },
    };
  } catch (error) {
    return {
      props: {
        notifications: { notifications: [], unreadCount: 0 },
      },
    };
  }
}
```

#### App Router 예시
```typescript
// app/notifications/page.tsx
import { notificationApi } from '@/lib/api/client';

export default async function NotificationsPage() {
  const { data } = await notificationApi.getNotifications();
  
  return (
    <div>
      <h1>알림 ({data.data.unreadCount}개 읽지 않음)</h1>
      {data.data.notifications.map((item) => (
        <div key={item.notificationId}>
          <h3>{item.title}</h3>
          <p>{item.content}</p>
        </div>
      ))}
    </div>
  );
}
```

### 4. React Query 통합 (선택사항)

#### 설치
```bash
npm install @tanstack/react-query
```

#### Custom Hook 작성
```typescript
// src/hooks/useNotifications.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '@/lib/api/client';

export const useNotifications = () => {
  return useQuery({
    queryKey: ['notifications'],
    queryFn: async () => {
      const { data } = await notificationApi.getNotifications();
      return data.data;
    },
  });
};

export const useDeleteNotification = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (notificationId: number) => 
      notificationApi.deleteNotification(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
};
```

#### 컴포넌트에서 사용
```typescript
// components/NotificationList.tsx
import { useNotifications, useDeleteNotification } from '@/hooks/useNotifications';

export function NotificationList() {
  const { data: notifications, isLoading } = useNotifications();
  const deleteNotification = useDeleteNotification();

  if (isLoading) return <div>Loading...</div>;

  return (
    <div>
      {notifications?.notifications.map((item) => (
        <div key={item.notificationId}>
          <h3>{item.title}</h3>
          <button onClick={() => deleteNotification.mutate(item.notificationId)}>
            삭제
          </button>
        </div>
      ))}
    </div>
  );
}
```

## 생성된 코드 구조

```
src/lib/api/generated/
├── api/
│   ├── notification-api.ts    # API 클라이언트 클래스
│   └── index.ts
├── models/
│   ├── notification-item.ts   # 타입 정의
│   ├── notification-items.ts
│   └── index.ts
├── base.ts                    # Axios 기본 설정
└── configuration.ts           # API 설정 클래스
```

## 커스터마이징

### 1. 인터셉터 추가
```typescript
// src/lib/api/interceptors.ts
import { AxiosInstance } from 'axios';

export function setupInterceptors(axios: AxiosInstance) {
  // 요청 인터셉터
  axios.interceptors.request.use(
    (config) => {
      console.log('API Request:', config.url);
      return config;
    },
    (error) => Promise.reject(error)
  );

  // 응답 인터셉터
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // 토큰 만료 처리
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );
}
```

### 2. 에러 핸들링
```typescript
// src/lib/api/error-handler.ts
import { AxiosError } from 'axios';
import { GrobleResponse } from './generated';

export function handleApiError(error: unknown): string {
  if (error instanceof AxiosError) {
    const grobleError = error.response?.data as GrobleResponse<unknown>;
    return grobleError?.message || '알 수 없는 오류가 발생했습니다.';
  }
  return '네트워크 오류가 발생했습니다.';
}
```

## 베스트 프랙티스

1. **환경 변수 사용**
   ```typescript
   // .env.local
   NEXT_PUBLIC_API_URL=https://api.groble.com
   ```

2. **타입 가드 사용**
   ```typescript
   function isNotificationItem(item: unknown): item is NotificationItem {
     return (item as NotificationItem).notificationId !== undefined;
   }
   ```

3. **에러 바운더리 적용**
   ```typescript
   // components/ErrorBoundary.tsx
   import { ErrorBoundary } from 'react-error-boundary';
   
   function ErrorFallback({ error }: { error: Error }) {
     return <div>에러: {error.message}</div>;
   }
   ```

4. **로딩 상태 관리**
   ```typescript
   const [isLoading, setIsLoading] = useState(false);
   
   const fetchData = async () => {
     setIsLoading(true);
     try {
       const data = await notificationApi.getNotifications();
       // ...
     } finally {
       setIsLoading(false);
     }
   };
   ```

## 자동화 CI/CD

### GitHub Actions 예시
```yaml
# .github/workflows/generate-sdk.yml
name: Generate Client SDK

on:
  push:
    paths:
      - 'groble-api/groble-api-spec/openapi/**'

jobs:
  generate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          
      - name: Generate SDK
        run: ./gradlew :groble-api:groble-api-spec:generateTypeScriptClient
        
      - name: Create PR
        uses: peter-evans/create-pull-request@v5
        with:
          title: 'Update Client SDK'
          branch: update-client-sdk
          commit-message: 'chore: update generated client SDK'
```

## 트러블슈팅

### 1. CORS 에러
- 백엔드에서 CORS 설정 확인
- Next.js API Routes를 프록시로 사용

### 2. 타입 불일치
- OpenAPI 명세서와 실제 API 응답 비교
- `strict: true` TypeScript 설정 확인

### 3. 빌드 오류
- `node_modules` 삭제 후 재설치
- TypeScript 버전 호환성 확인
