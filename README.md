# Gradle에서 빌드 프로필 지정하여 빌드하기

Gradle을 이용해서 환경별로 별도의 빌드 결과물을 얻을 수 있도록 스크립트를 짜 보았다.

## 기본 아이디어

일반적으로 Gradle을 이용해서 개발을 할 경우, ```src/main/resources``` 경로에 환경설정 리소스를 넣어두고 개발하는 경우가 많다. 이 점을 이용해서, 개발할때는 ```src/main/resources``` 경로의 리소스를 사용해서 개발하다가, 특정 빌드 환경변수가 주어진 채로 빌드하는 경우에는 ```src/main/resources-product``` 등의 별도의 리소스 경로를 이용해서 빌드되게 한다.

## 폴더 구조

폴더 구조는 다음과 같다.

```
src
  /main
    /java
        - 자바 소스 파일
    /resources-development
        - 개발시 사용될 리소스들 (환경설정 포함)
    /resources-product
        - 배포시 사용될 리소스들 (환경설정 포함)
```

```build.gradle``` 파일에 다음 구문을 삽입한다

```
def profile = project.getProperties().get("profile")
if(!project.hasProperty("profile") || !profile) profile = "development"

sourceSets {
    main {
        java {
            srcDirs = ["src/main/java"]
        }
        resources {
            srcDirs = ["src/main/resources-${profile}]
        }
        resources.srcDirs.each { dir -> 
            if(!dir.exists())
                throw new RuntimeException("Resource folder [${dir}] is not exists!")
        }
    }
}
```

```build.gradle``` 파일과 같은 경로에 ```gradle.properties``` 파일을 만든다. Gradle은 빌드시 ```gradle.properties``` 파일이 ```build.gradle``` 파일과 동일한 경로에 존재하면 이 파일에 들어있는 값들을 빌드 환경 변수로 등록하고 빌드한다. ```build.properties``` 파일에 아래 내용을 저장한다.

```
profile=product
```

이렇게 하면 빌드할 때 빌드 환경변수 profile에 product라는 값을 넘겨주고 빌드를 시작하게 한다.

이후 빌드할때는 ```gradle.properties``` 파일의 profile 필드의 값을 고치거나, ```-Pprofile=product``` 옵션을 줘서 ```gradlew run``` 등으로 실행한다. ```gradle.properties``` 파일에 저장된 내용과 ```-P``` 옵션의 빌드 변수 값이 동시에 지정되는 경우, ```gradle.properties``` 파일의 해당 값은 무시된다.

아래 예제들은 모두 ```-P``` 옵션을 명시하지만, ```-P``` 옵션을 생략하고 ```gradle.properties``` 파일을 사용해도 동일하다.

```
# ./gradlew run -Pprofile=development        # build profile is development
It is now a development environment

# ./gradlew run -Pprofile=product            # build profile is product
It is now a product environment
```

빌드 프로필을 추가하기 위해서는 ```src/main/resources=${build-profile}``` 디렉토리를 새로 만들어서 필요한 리소스 파일들을 준비하고 빌드한다.

```
# mkdir src/main/resources-newprofile
# cp -r src/main/resources-development/. src/main/resources-newprofile
# vi src/main/resources-newprofile/kr/ayukawa/buildprofile/resources.properties
# ./gradlew run -Pprofile=newprofile
It is now a new profile!
```

리소스 디렉토리가 생성되지 않은 프로필로 빌드하려고 하는 경우 예외가 발생하면서 빌드가 거부된다

```
# ./gradlew run -Pprofile=notexistprofile
FAILURE: Build failed with an exception

* Where:
Build file '~/gradle-build-profile/build.gradle' line: 33

* What went wrong:
A Problem occurred evaluating root project 'gradle-build-profile'.
> Resource folder [~/gradle-build-profile/src/main/resources-notexistprofile] is not exists!

(...후략...)
```

## 추가 이슈

### 각 빌드 프로필 별 공통 리소스가 많을때

각 프로필 별로 공통 리소스가 많고 환경 파일 한두개 정도만 다를 경우, sourceSet 설정시에 리소스 디렉토리를 공통 리소스 디렉토리 하나와 프로필별 개별 디랙토리 하나를 추가해서 설정하도록 한다.

```
sourceSets {
    main {
        java {
            srcDirs = ["src/main/java"]
        }
        resources {
            // "src/main/resources" 디렉토리에는 프로필별 공통 리소스가,
            // "src/main/resources-${profile}" 디렉토리에는 빌드별로 구분되어야 할 환경설정 등을 저장한다
            srcDirs = ["src/main/resources", "src/main/resources-${profile}"]
        }
    }
}
```
