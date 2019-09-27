package com.github.frimtec.android.securesmsproxyapi;

interface ISecureSmsProxyService {

    String version();

    boolean isAllowed(in String applicationName, in List<String> phoneNumbers);

}
