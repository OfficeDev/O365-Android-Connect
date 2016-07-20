# Android 用 Office 365 Connect のサンプル
[![Build Status](https://travis-ci.org/OfficeDev/O365-Android-Connect.svg?branch=master)](https://travis-ci.org/OfficeDev/O365-Android-Connect)

[日本 (日本語)](/loc/README-ja.md) (日本語)

[![Office 365 Connect のサンプル](/readme-images/O365-Android-Connect-video_play_icon.png)](https://www.youtube.com/watch?v=3IQIDFrqhY4 "稼働中のサンプルを確認するにはこちらをクリックしてください")

Office 365 への接続は、各 Android アプリが Office 365 のサービスおよびデータの操作を開始するために必要な最初の手順です。このサンプルは、1 つの API に接続してから呼び出す方法を示しています。

## デバイスの要件

Connect のサンプルを実行するには、デバイスが次の要件を満たしている必要があります。

* 画面のサイズが 800 x 480 以上である。
* Android の API レベルが 15 以上である。
 
## 前提条件

Android 用 Office 365 Connect のサンプルを使用するには以下が必要です。

* [Android Studio](http://developer.android.com/sdk/index.html) バージョン 1.0 以上。
* [Java 開発キット (JDK) 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)。
* Office 365 アカウント。 [Office 365 開発者プログラムに参加し、Office 365 の 1 年間の無料サブスクリプションを取得](https://aka.ms/devprogramsignup)しましょう。それには Office 365 アプリの構築を開始するために必要なリソースも含まれています。

     > 注:サブスクリプションが既に存在する場合、上記のリンクをクリックすると、*申し訳ありません、現在のアカウントに追加できません* と表示されたページに移動します。その場合は、現在使用している Office 365 サブスクリプションのアカウントをご利用いただけます。
     Office 365 にすでにサインインしている場合、上記のリンクの [サインイン] ボタンに、*申し訳ございません。お客様のリクエストを処理できません。* というメッセージが表示されます。その場合、その同じページで Office 365 からサインアウトし、その後もう一度サインインしてください。

* アプリケーションを登録する Microsoft Azure テナント。Azure Active Directory は、アプリケーションが認証と承認に使用する ID サービスを提供します。ここでは、試用版サブスクリプションを取得できます。[Microsoft Azure](https://account.windowsazure.com/SignUp)。

     > 重要事項：Azure サブスクリプションが Office 365 テナントにバインドされていることを確認する必要があります。確認するには、Active Directory チームのブログ投稿「[複数の Windows Azure Active Directory を作成および管理する](http://blogs.technet.com/b/ad/archive/2013/11/08/creating-and-managing-multiple-windows-azure-active-directories.aspx)」を参照してください。「**新しいディレクトリを追加する**」セクションで、この方法について説明しています。また、詳細については、「[Office 365 開発環境をセットアップする](https://msdn.microsoft.com/office/office365/howto/setup-development-environment#bk_CreateAzureSubscription)」や「**Office 365 アカウントを Azure AD と関連付けてアプリを作成および管理する**」セクションも参照してください。
      
* Azure に登録されたアプリケーションのクライアント ID とリダイレクト URI の値。アプリケーションには、**[ユーザーとしてメールを送信する]** アクセス許可を付与する必要があります。[Azure にネイティブ クライアント アプリケーションを追加](https://msdn.microsoft.com/office/office365/HowTo/add-common-consent-manually#bk_RegisterNativeApp)し、[適切なアクセス許可を付与](https://github.com/OfficeDev/O365-Android-Connect/wiki/Grant-permissions-to-the-Connect-application-in-Azure)します。

## Android Studio を使用してサンプルを開く

1. developer.android.com の[指示](http://developer.android.com/sdk/installing/adding-packages.html)に従って、[Android Studio](http://developer.android.com/tools/studio/index.html#install-updates) をインストールし、Android SDK パッケージを追加します。
2. このサンプルをダウンロードするか、クローンを作成します。
3. Android Studio を起動します。
	1. 開いているプロジェクトをすべて閉じ、 **[既存のAndroid Studio プロジェクトを開く]** を選択します。
	2. ローカル リポジトリを参照し、O365-Android-Connect プロジェクトを選択します。**[OK]** をクリックします。

	> 注:Android Studio は、Gradle ラッパーを使用するかどうかを尋ねるダイアログを表示する場合があります。**[OK]** をクリックします。
	>  
	> また、**Android サポート リポジトリ**がインストールされていない場合、Android Studio は**フレームワーク検出**の通知を表示します。フレームワーク検出の通知が表示されないようにするには、SDK マネージャーを開き、Android サポート リポジトリを追加してください。
4. [```Constants.java```](app/src/main/java/com/microsoft/office365/connect/Constants.java) ファイルを開きます。
	1. [```CLIENT_ID```](app/src/main/java/com/microsoft/office365/connect/Constants.java#L12) 定数を検索して、その String 値を Azure Active Directory に登録されているクライアント ID と同じ値に設定します。
	2. [```REDIRECT_URI```](/app/src/main/java/com/microsoft/office365/connect/Constants.java#L13) 定数を検索して、その String 値を Azure Active Directory に登録されているリダイレクト URI と同じ値に設定します。
    ![Office 365 Connect sample](/readme-images/O365-Android-Connect-Constants.png "Client ID and Redirect URI values in Constants file")

    > 注:CLIENT\_ID と REDIRECT\_URI の値がない場合は、[ネイティブ クライアント アプリケーションを Azure に追加](https://msdn.microsoft.com/ja-jp/library/azure/dn132599.aspx#BKMK_Adding)し、CLIENT\_ID と REDIRECT_URI を書き留めます。

Connect のサンプルをビルドしたら、エミュレーターまたはデバイス上で実行できます。**[デバイスの選択]** ダイアログから API レベル 15 以上のデバイスを選択してください。

サンプルの詳細については、Wiki ページ[コードを理解する](https://github.com/OfficeDev/O365-Android-Connect/wiki/Understanding-the-Connect-sample-code)にアクセスしてください。このコード サンプルをアプリで使用する場合は、「[O365 Android Connect サンプル コードをアプリで使用する](https://github.com/OfficeDev/O365-Android-Connect/wiki/Using-the-O365-Android-Connect-sample-code-in-your-app)」にアクセスしてください。

## 質問とコメント

O365 Android Connect プロジェクトについて、Microsoft にフィードバックをお寄せください。質問や提案につきましては、このリポジトリの「[問題](https://github.com/OfficeDev/O365-Android-Connect/issues)」セクションに送信できます。

Office 365 開発全般の質問につきましては、「[スタック オーバーフロー](http://stackoverflow.com/questions/tagged/Office365+API)」に投稿してください。質問またはコメントには、必ず [Office365] および [API] のタグを付けてください。

## 次の手順

このサンプルでは、アプリが Office 365 を使用して操作する必要がある重要項目のみを示しています。アプリが Office 365 API を使用してできることがさらに多数あります。たとえば、ユーザーが予定表で作業日を管理できるようにする、ユーザーが OneDrive に保存したすべてのファイルで必要な情報を検索する、または連絡先のリストからユーザーが必要とする人を正確に見つけるなどです。「[Office 365 API スタート プロジェクト (Android 用)](https://github.com/officedev/O365-Android-Start/)」でさらに説明しています。お客様のアイデアを刺激するお役に立つことができればと思います。 
  
## その他の技術情報

* [Office 365 API プラットフォームの概要](https://msdn.microsoft.com/office/office365/howto/platform-development-overview)
* [Office 365 SDK for Android](https://github.com/OfficeDev/Office-365-SDK-for-Android)
* [アプリで Office 365 API の使用を開始する](https://msdn.microsoft.com/office/office365/howto/getting-started-Office-365-APIs)
* [Office 365 API スタート プロジェクトおよびサンプル コード](https://msdn.microsoft.com/office/office365/howto/starter-projects-and-code-samples)
* [Android 用 Office 365 コード スニペット](https://github.com/OfficeDev/O365-Android-Snippets)
* [Android 版 Office 365 API スタート プロジェクト](https://github.com/OfficeDev/O365-Android-Start)
* [Android 用 Office 365 プロファイル サンプル](https://github.com/OfficeDev/O365-Android-Profile)

## 著作権
Copyright (c) 2015 Microsoft.All rights reserved.
