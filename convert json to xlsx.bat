java -jar -Duser.language=ru -Duser.region=RU build/libs/neural_network-1.0.jar ^
--convert-from-type json ^
--convert-to-type xlsx ^
--model C:\sources\neural_network\src\test\resources\test_model_real.json ^
--convert-result C:\sources\neural_network\build\converted.xlsx