# Database to CSV conversion
The mission of this project, is to create a generic solution to extract database table, to be a CSV. Can run in limited memory, without OOM.


## Security Approach
> Use RSA to encrypt and decrypt the DB password.  
> - Use public key to encrypt `DB password`. (The config file stores the encrypted password.)
> - Use private key to decrypt `DB password`.
> - The private key is stored in `PKCS12` keystore, protected by `keystore password`.
> - The `keystore password` is obscured with an `XOR key`, and split into 2 parts, stored in 2 folders separately.
> - The `XOR key` is encoded in BASE64, and stored in a separated folder.
> - The application provides command line entry to encrypt password. (No decryption option), it is for IAM team usage.


## Supporte Databases
- MySQL (Value as MySQL)
- PostgresQL (Value as "POSTGRESQL")
- Oracle (Value as "ORACLE", Not yet test for performance)
- MS SQL Server (Value as "SQLSERVER")

### Usage
> **You should deploy in three steps:**  
  - First time deploy, run init function first. (One-off work on one server)  
    `java -jar xxx.jar -init <app_id>`  
  - Ask your IAM team to encrypt the password for you  
    `java -jar xxx.jar -encrypt <your db password>`  
  - When you got the encrypted password, input to the config.xlsx  
    `Well configure your config.xlsx`
  - Run/Schedule the program:  
    `java -jar xxx.jar /home/wasadm/config.xlsx`

> To define a "today" in testing environment, or back date processing.
```
java -DbusinessDate=20200205 -jar xxx.jar /home/wasadm/config.xlsx 
```

### Details core logic please ref to the test cases
```
    @Test
    public void testSqlServerConvert() throws SQLException, IOException {

        DatabaseTableToCSV converter = new DatabaseTableToCSV(mssserverUrl,
                "SqlServer",
                mssserverUsr,
                mssserverPsw);
        try {
            converter.convertTable("test", "/tmp/test.csv");
            converter.convertWithCustomSql("select * from test where id <= 100001", "/tmp/test1.csv");
        } finally {
            converter.close();
        }
    }
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