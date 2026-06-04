import { BatchTranslator, TranslatorBacking } from "./translator.js";

class FeederBacking extends TranslatorBacking {
    async loadModelRegistery() {
        return this.options.modelRegistry;
    }

    async loadTranslationModel({ from, to }) {
        const entries = (await this.registry).filter((model) => model.from === from && model.to === to);
        if (!entries.length) {
            throw new Error(`No app model for ${from} to ${to}`);
        }

        const files = entries[0].files;

        // Helper to fetch a file via XHR since WebView fetch() doesn't support file://
        const fetchFile = (url) => new Promise((resolve, reject) => {
            const xhr = new XMLHttpRequest();
            xhr.open('GET', url, true);
            xhr.responseType = 'arraybuffer';
            xhr.onload = () => {
                if (xhr.status === 200 || xhr.status === 0) {
                    resolve(xhr.response);
                } else {
                    reject(new Error(`XHR failed: status=${xhr.status}`));
                }
            };
            xhr.onerror = () => reject(new Error(`XHR network error for ${url}`));
            xhr.send();
        });

        const buffers = Object.fromEntries(await Promise.all(Object.entries(files).map(async ([part, file]) => {
            if (!file || !file.url) {
                return [part, null];
            }
            const buffer = await fetchFile(file.url);
            return [part, buffer];
        })));

        const vocabs = buffers.vocab ? [buffers.vocab] : [buffers.srcvocab, buffers.trgvocab].filter(Boolean);
        if (!vocabs.length) {
            throw new Error(`No vocabulary file for ${from} to ${to}`);
        }

        const config = {};
        if (files.model.name.endsWith("intgemm8.bin")) {
            config["gemm-precision"] = "int8shiftAll";
        }
        if (files.qualityModel) {
            config["skip-cost"] = false;
        }
        if (files.config) {
            Object.assign(config, files.config);
        }

        return {
            model: buffers.model,
            shortlist: buffers.lex,
            vocabs,
            qualityModel: buffers.qualityModel,
            config,
        };
    }
}

let translator = null;

window.FeederBergamot = {
    initialize(modelRegistry) {
        if (translator) {
            translator.delete();
        }
        const options = {
            modelRegistry,
            pivotLanguage: "en",
            workers: 1,
            batchSize: 4,
            cacheSize: 16384,
            downloadTimeout: 0,
            useNativeIntGemm: false,
            onerror: (error) => {
                const msg = error && error.message ? error.message :
                    (error && error.target && error.target.message ? error.target.message :
                    String(error));
                window.AndroidBergamot.onLog(msg);
            },
        };
        translator = new BatchTranslator(options, new FeederBacking(options));
    },

    async translate(id, from, to, text, html) {
        try {
            if (!translator) {
                throw new Error("Bergamot translator is not initialized");
            }
            const response = await translator.translate({
                from,
                to,
                text,
                html,
                qualityScores: false,
            });
            window.AndroidBergamot.onTranslationSuccess(id, response.target.text);
        } catch (error) {
            window.AndroidBergamot.onTranslationError(id, String(error && error.message ? error.message : error));
        }
    },

    async translateBatch(id, from, to, texts, html) {
        try {
            if (!translator) {
                throw new Error("Bergamot translator is not initialized");
            }
            const responses = await Promise.all(texts.map((text) => translator.translate({
                from,
                to,
                text,
                html,
                qualityScores: false,
            })));
            window.AndroidBergamot.onTranslationBatchSuccess(id, JSON.stringify(responses.map((response) => response.target.text)));
        } catch (error) {
            window.AndroidBergamot.onTranslationError(id, String(error && error.message ? error.message : error));
        }
    },
};

window.AndroidBergamot.onReady();
