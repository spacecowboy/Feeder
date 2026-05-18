#include <jni.h>
#include <string>

#ifdef BERGAMOT_AVAILABLE
#include "translator/translation_model.h"
#include "translator/service.h"
#endif

extern "C" JNIEXPORT jstring JNICALL
Java_com_nononsenseapps_feeder_bergamot_BergamotTranslator_nativeTranslate(
        JNIEnv *env,
        jobject /* this */,
        jstring modelDir,
        jstring text,
        jboolean preserveHtml) {

#ifdef BERGAMOT_AVAILABLE
    const char *modelDirStr = env->GetStringUTFChars(modelDir, nullptr);
    const char *textStr = env->GetStringUTFChars(text, nullptr);

    std::string modelPath = std::string(modelDirStr) + "/model.bin";
    std::string lexPath = std::string(modelDirStr) + "/lex.bin";
    std::string vocabPath = std::string(modelDirStr) + "/vocab.spm";

    marian::bergamot::AsyncService::Config serviceConfig;
    marian::bergamot::AsyncService service(serviceConfig);

    auto options = marian::bergamot::parseOptionsFromFilePath(modelPath, vocabPath, lexPath);
    auto model = std::make_shared<marian::bergamot::TranslationModel>(options, /*replicas=*/1);

    marian::bergamot::ResponseOptions responseOptions;
    responseOptions.HTML = static_cast<bool>(preserveHtml);

    std::string input(textStr);
    std::string result;

    service.translate(model, std::move(input), [&result](auto &&response) {
        result = response.target.text;
    });

    env->ReleaseStringUTFChars(modelDir, modelDirStr);
    env->ReleaseStringUTFChars(text, textStr);

    return env->NewStringUTF(result.c_str());
#else
    return env->NewStringUTF("");
#endif
}
