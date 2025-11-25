FROM amazoncorretto:17
#antivirus
WORKDIR project/

# Install Build Essentials
RUN yum -y update \
    && yum -y group install 'Development Tools' \
    && yum -y install coreutils

RUN yum -y update && \
    yum install -y python3 && \
    yum clean all

RUN python3 --version
RUN pip3 install msal requests
# Set Environment Variables
ENV SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
    ANDROID_HOME="/usr/local/android-sdk" \
    ANDROID_VERSION=34

# Download Android SDK
RUN mkdir -p "$ANDROID_HOME/cmdline-tools" .android \
    && cd "$ANDROID_HOME/cmdline-tools" \
    && curl -o sdk.zip $SDK_URL \
    && unzip sdk.zip \
    && rm sdk.zip \
    && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license"
RUN mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest | true && yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses

# Install Android Build Tool and Libraries
RUN $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --update
RUN $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "build-tools;30.0.3"
RUN $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-${ANDROID_VERSION}"
RUN $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools"
RUN mkdir /home/.sonar && chmod 777 /home/.sonar
ENV SONAR_USER_HOME=/home/.sonar
RUN cp /usr/local/android-sdk/build-tools/30.0.3/zipalign /bin/ && cp /usr/local/android-sdk/build-tools/30.0.3/lib64/* /lib64/ && chmod 777 /bin/zipalign

# Adjust permissions for Android SDK
RUN chown -R root:root $ANDROID_HOME && chmod -R 777 $ANDROID_HOME

RUN groupadd -g 1000 unplugged && adduser -g unplugged -u 1000 unplugged
CMD ["/bin/bash"]