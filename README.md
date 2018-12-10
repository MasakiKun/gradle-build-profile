# Gradle에서 빌드 프로필 지정하여 빌드하기

Gradle을 이용해서 환경별로 별도의 빌드 결과물을 얻을 수 있도록 스크립트를 짜 보았다.

## 기본 아이디어

일반적으로 Gradle을 이용해서 개발을 할 경우, ```src/main/resources``` 폴더에 환경설정 리소스를 넣어두고 개발하는 경우가 많다. 이 점을 이용해서, 아예 개발할때는 ```src/main/resources``` 경로의 리소스를 사용해서 개발하다가, 특정 조건으로 빌드하게 되는 경우에는 ```src/main/release-product``` 등의 별도의 리소스 경로를 이용해서 빌드되게 한다.

## 이슈사항
인터넷에 있는 내용들을 조합해서 빌드 스크립트를 짜 봤는데, 현재 개발환경에서 사용하기에 아래와 같은 이슈사항이 있었다.

  * 인터넷에서 설명된 대다수의 설명은 -P 옵션으로 빌드 환경변수를 전달하도록 되어있다. 그런데 Eclipse나 IntelliJ에서는 현재 프로젝트의 빌드스크립트의 Task 목록을 선택해서 실행하게 되어있다. 기능상 딱히 인자를 받지 않으며, 해당 기능까지 찾아가며 실행하기에는 번거로운 느낌도 든다.
    * ```build.gradle``` 파일과 동일한 경로에 ```gradle.properties``` 파일을 만들어두면 Gradle이 시작될 때 해당 파일에 있는 값들을 빌드 환경 변수로 추가해주므로, ```-P``` 옵션 대신 이 파일을 사용한다.
  * 일반적으로 ```-P``` 옵션으로 전달된 빌드 환경 변수와 경로를 조합해서 SourceSets를 지정하는 방법을 사용한다. 이때 환경변수 값에 오타라도 내면 잘못된 경로가 지정되게 되므로, 빌드는 성공하겠지만 잘못된 빌드 결과물이 생성되고, 최종적으로 빌드 결과물이 리소스를 포함하지 않으므로 구동에 실패한다.
    * SourceSets를 설정하는 단계에서 해당하는 경로의 존재 여부를 확인하여, 잘못된 경로인 경우 예외를 반환하고 빌드를 거부하게 한다.

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

```build.gradle``` 파일과 같은 경로에 ```gradle.properties``` 파일을 만든다. Gradle은 빌드시 이 파일이 ```build.gradle``` 파일과 동일한 경로에 존재하면 이 값들을 빌드 환경 변수로 등록해준다. ```build.properties``` 파일에 아래 내용을 저장한다.

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

### 각 빌드 프로필 별 공통 리소스가 많을떄

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
