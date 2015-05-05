# shiro-aad
Apache Shiro authentication and authorization realm for Azure Active Directory (AAD)
====================================================================================

This is a Shiro authentication and authorization realm for Azure Active Directory (AAD).

It uses the adal4j library for authentication and the Graph API for retrieving authorization data i.e. group and role memberships.

Azure management
----------------

In order to provide the realm access to your Azure directory, you must configure two applications for your directory in the Azure management console:

* One "native" type application for doing the authentication. Only a Client ID is possible for a "native" application.

* One "web" application for doing the authorization. For a "web" application you'll have both a Client ID and a Client Secret (which you must create in the Azure management console).

Configuration
-------------

There are several ways to configure the AAD realm.

To configure the realm using the standard Shiro "ini" file mechanism, you'll need to create the configuration object, set any of its properties, then create the realm itself and set the configuration to it. For example, something like the following in shiro.ini:

    [main]
    aadRealmConfig = com.nitorcreations.willow.shiro.aad.AADRealmConfig
    aadRealmConfig.authority = https://login.windows.net/
    aadRealmConfig.tenant = contoso.onmicrosoft.com
    aadRealmConfig.graphResource = https://graph.windows.net/
    aadRealmConfig.authenticationClientId = 12345678-abcd-5678-4567-889900122334
    aadRealmConfig.authorizationClientId = 98765432-4321-abcd-abcd-8899aabcdcdd
    aadRealmConfig.authorizationClientSecret = 8FnpGBmR4Do8GB4BbumFtGvZwKdBbjKsrWr2QsS3k/w=
    
    myRealm = com.nitorcreations.willow.shiro.aad.AADRealm
    myRealm.config = $aadRealmConfig

Alternatively, the AADRealm object can be created completely programmatically and the properties of the AADRealmConfig be set explicitly. It is also possible to load the properties of the AADRealmConfig from a Properties file or using Shiro's ResourceUtils.getInputStreamForPath() mechanism.
