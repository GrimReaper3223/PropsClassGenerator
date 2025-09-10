export GPG_TTY=$(tty)
gpg-connect-agent reloadagent /bye
mvn clean package install -U -Dmaven.test.skip=true -T 4