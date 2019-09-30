package com.github.frimtec.android.securesmsproxyapi;

interface ISecureSmsProxyService {

    boolean isAllowed(in String applicationName, in List<String> phoneNumbers);

}
