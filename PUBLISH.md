# Generate GPG signing key
https://www.droidcon.com/2024/07/25/publish-on-maven-central/

```commandline
gpg --gen-key
```
> Provide name, email, a new passphrase

> last 8 digits of key is key ID
> 
> Looks something like this: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA12345678

## gradle.properties: (ignored by git)
```
signing.keyId=12345678
signing.password=***
signing.secretKeyRingFile=gpg/secring.kbx
```

Store password in gradle.properties:
 
Store key on keyserver:

```commandline
gpg --keyserver keyserver.ubuntu.com --send-keys AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA12345678
gpg --export-secret-keys --output ./gpg/secring.kbx
```

