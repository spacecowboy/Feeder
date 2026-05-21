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
        const buffers = Object.fromEntries(await Promise.all(Object.entries(files).map(async ([part, file]) => {
            if (!file || !file.url) {
                return [part, null];
            }
            const response = await fetch(file.url);
            if (!response.ok) {
                throw new Error(`Could not load ${part} for ${from} to ${to}`);
            }
            return [part, await response.arrayBuffer()];
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
                window.AndroidBergamot.onLog(String(error && error.message ? error.message : error));
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
