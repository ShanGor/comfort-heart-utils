# Comfort Heart Utilities
To build some utility command java methods, to reduce repeated work in the futher.


## Security Approach
> Use RSA to encrypt and decrypt the DB password.  
> - Use public key to encrypt `DB password`. (The config file stores the encrypted password.)
> - Use private key to decrypt `DB password`.
> - The private key is stored in `PKCS12` keystore, protected by `keystore password`.
> - The `keystore password` is obscured with an `XOR key`, and split into 2 parts, stored in 2 folders separately.
> - The `XOR key` is encoded in BASE64, and stored in a separated folder.
> - The application provides command line entry to encrypt password. (No decryption option), it is for IAM team usage.

## Sequential Job Runner Usage
### Variables
All below variables will be set to the system env while run the command of the job. And the variables in the command line, can be interpolated. 
- All the variables in System.env
- All the variables in System.getProperties (If exist in original System.env, override it)
- All the variables defined in the Sheet "Config" of the Jobs.xlsx (Override the above 2)
```
# For below shell script test.sh
------------
echo "DB Password is: $PASSWORD"
echo "Order date is: $1"
------------

# call it like this 
$ sh test.sh $ODATE

# Before call this java tool "Sequential Job Runner",
# Set system variable with 
$ export ODATE=200302

# Configure the variable PASSWORD in the Job.xlsx, call it
$ java -cp target/comfort-heart-util-1.0-SNAPSHOT-jar-with-dependencies.jar tech.comfortheart.app.JobRunner $JOB_GROUP /xxxx/Jobs.xlsx

# Result is:
DB Password is: hey
Order date is: 200302

```

### Steps:
 - Initialize the crypto keys.     
```
java -cp target/comfort-heart-util-1.0-SNAPSHOT-jar-with-dependencies.jar tech.comfortheart.app.JobRunner -init my-app
```
 - Encrypt your password if needed.
 ``` 
 java -cp target/comfort-heart-util-1.0-SNAPSHOT-jar-with-dependencies.jar tech.comfortheart.app.JobRunner -encrypt my-password
```
> - Run the jobs in the job group, with the specified excel file. (can find sample as `src/test/resources/Jobs.xlsx`)
```
java -cp target/comfort-heart-util-1.0-SNAPSHOT-jar-with-dependencies.jar tech.comfortheart.app.JobRunner $JOB_GROUP /xxxx/Jobs.xlsx
```

## Sample RSA Generation
> The program use Java code to generate instead of keytool command. This is for developer reference only.
```
# Generate the keystore with self-signed keypair.
keytool -genkeypair -alias E2E_Alias -sigalg SHA256withRSA -keystore demo.jks -storetype PKCS12 -keysize 2048 -keyalg RSA -dname "CN=Consumer,OU=TechDept,O=Comfortheart.tech,L=GZ,ST=GD,C=China" -storepass changeit -keypass changeit

# Export the public key.
keytool -exportcert -keystore demo.jks -file demo.cer -alias E2E_Alias -storepass changeit -keypass changeit
```

# License
MIT, All rights reserved by Samuel Chan