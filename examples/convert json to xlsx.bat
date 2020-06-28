java -jar -Duser.language=ru -Duser.region=RU libs/train-@project.version@.jar ^
--convert-from-type json ^
--convert-to-type xlsx ^
--model input\test_model_real.json ^
--convert-result output\converted.xlsx