FROM fedora:35

SHELL ["/bin/bash", "-c"]

RUN dnf -y update && \
    dnf -y install \
        curl \
        diffutils \
        dos2unix \
        git \
        gnupg \
        java-17-openjdk-devel \
        java-17-openjdk-src \
        maven \
        maven-openjdk17 \
        openssh-clients \
        patch \
        procps-ng \
        vim-enhanced

RUN useradd -m -g users -c 'VSCode User' vscode

USER vscode
ENTRYPOINT ["/bin/bash", "--login"]
