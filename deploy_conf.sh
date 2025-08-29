export GPG_TTY=$(tty)
gpg-connect-agent reloadagent /bye
mvn --settings settings.xml clean deploy -U -Dmaven.test.skip=true -T 4

