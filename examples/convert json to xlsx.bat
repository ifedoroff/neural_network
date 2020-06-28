java -jar -Duser.language=ru -Duser.region=RU libs/train-@project.version@.jar ^
--convert-from-type json ^
--convert-to-type xlsx ^
--model input\trained_model.json ^
--convert-result output\converted.xlsx